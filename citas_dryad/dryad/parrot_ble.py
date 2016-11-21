from bluepy.btle import DefaultDelegate, Peripheral, UUID
import datetime
import traceback
import numpy as np
from collections import defaultdict
from utils import transform
import logging


## Constants ##
# Services
SERVICES = {
	"LIVE"			: "39e1fa0084a811e2afba0002a5d5c51b",
	"BATTERY"		: 0x180f,
	"DEVICE_INFO"	: 0x180a
}

# Sensors Characteristics (Old firmware)
SENSORS = {
	"SUNLIGHT"		: "39e1fa0184a811e2afba0002a5d5c51b",
	"SOIL_EC"		: "39e1fa0284a811e2afba0002a5d5c51b",
	"SOIL_TEMP"		: "39e1fa0384a811e2afba0002a5d5c51b",
	"AIR_TEMP"		: "39e1fa0484a811e2afba0002a5d5c51b",
	"SOIL_MOISTURE"	: "39e1fa0584a811e2afba0002a5d5c51b",
}

# Sensors Characteristics (New firmware)
CAL_SENSORS = {
	"SUNLIGHT"		: "39e1fa0184a811e2afba0002a5d5c51b",
	"SOIL_EC"		: "39e1fa0284a811e2afba0002a5d5c51b",
	"SOIL_TEMP"		: "39e1fa0384a811e2afba0002a5d5c51b",
	"AIR_TEMP"		: "39e1fa0484a811e2afba0002a5d5c51b",
	"VWC"			: "39e1fa0584a811e2afba0002a5d5c51b",
	"CAL_VWC"		: "39e1fa0984a811e2afba0002a5d5c51b",
	"CAL_AIR_TEMP"	: "39e1fa0a84a811e2afba0002a5d5c51b",
	"CAL_DLI"		: "39e1fa0b84a811e2afba0002a5d5c51b",
	"CAL_EA"		: "39e1fa0c84a811e2afba0002a5d5c51b",
	"CAL_ECB"		: "39e1fa0d84a811e2afba0002a5d5c51b",
	"CAL_EC_POROUS"	: "39e1fa0e84a811e2afba0002a5d5c51b",
}

# Control characteristics
CONTROLS = {
	"FIRMWARE_VER"		: 0x2a26,
	"LIVE_MODE_PERIOD"	: "39e1fa0684a811e2afba0002a5d5c51b",   
	"LED"				: "39e1fa0784a811e2afba0002a5d5c51b",
	"LAST_MOVE_DATE"	: "39e1fa0884a811e2afba0002a5d5c51b", 
	"BATTERY_LEVEL"		: 0x2a19
}


# Parrot class	
class Parrot():	
	def __init__(self, device, name, n_read=3):
		self.name = name
		self.device = device
		self.peripheral = None
		self.n_read = n_read		
		self.live_measure_period = "\x01"
		self.isNewFirmware = True
		self.live_service = None
		self.battery_service = None
		self.logger = logging.getLogger("main.parrot_ble.Parrot")
	

	# turning on live measure period, 1s
	def set_live_measure_period(self):
		live_measure_ch = self.live_service.getCharacteristics(UUID(CONTROLS["LIVE_MODE_PERIOD"]))[0]
		live_measure_ch.write(str.encode(self.live_measure_period))

	# toggle of pf led		
	def switch_led(self, state):
		led_control_ch = self.live_service.getCharacteristics(UUID(CONTROLS["LED"]))[0]
		led_control_ch.write(str.encode(state))
	
	# returns whether parrot flower is the new version or not
	def checkFirmware(self, firmware_version):
		new_firmware_version = '1.1.0'
		ver_number = firmware_version.decode("utf-8").split("_")[1].split("-")[1]
		self.isNewFirmware = ver_number == new_firmware_version

	def add_timestamp(self, data):
		data["PF_TIMESTAMP"] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
		return data	
	

	# returns dictionary of sensor readings from parrot flower 
	def read_sensors(self, sensors=["SUNLIGHT", "SOIL_EC", "SOIL_TEMP", "AIR_TEMP", "VWC", "CAL_VWC", "CAL_AIR_TEMP", "CAL_DLI", "CAL_EA", "CAL_ECB", "CAL_EC_POROUS", "BATTERY"]):
		tr = transform.DataTransformation()
		self.logger.info("Starting sensor readings...")	
		
		reading = dict.fromkeys(sensors)

		battery_level_ch = self.battery_service.getCharacteristics(UUID(CONTROLS["BATTERY_LEVEL"]))[0]
		battery_level = 0
		
		try:
			# conversion from byte to decimal
			battery_level = ord(battery_level_ch.read())
			if "BATTERY" in reading.keys():
				reading["BATTERY"] = battery_level
		except Exception as err:
			self.logger.exception(traceback.print_tb(err.__traceback__))

		# iterate over the calibrated sensors characteristics
		for key, val in CAL_SENSORS.items():
			char = self.live_service.getCharacteristics(UUID(val))[0]	
			if char.supportsRead(): 
				if key == "SUNLIGHT":
					reading[key] = tr.conv_light(tr.unpack_U16(char.read()))
				elif key == "SOIL_EC":
					reading[key] = tr.conv_ec(tr.unpack_U16(char.read()))
				elif key in ["AIR_TEMP", "SOIL_TEMP"]:
					reading[key] = tr.conv_temp(tr.unpack_U16(char.read()))
				elif key == "VWC":
					reading[key] = tr.conv_moisture(tr.unpack_U16(char.read()))
				else:
					reading[key] = tr.decode_float32(char.read())
		
		return reading	

	# returns aggregated (averaged) readings
	def get_agg_readings(self):
		# getting readings for N_READ times (3 times)		
		readings = np.array()
		temp_counter = self.n_read
		while temp_counter != 0:
			readings = np.append(readings, self.read_sensors())
			temp_counter -= 1	
		
		aggregated_data = defaultdict(int)
		for entry in readings:	
			for key in entry.keys():
				aggregated_data[key] += float(entry[key])
		data = {k: v / self.n_read for k, v in aggregated_data.items()} 
		return self.add_timestamp(data)	
		
	def setup_conn(self): 
		self.logger.info("Attempting to connect to {} [{}]".format(self.name, self.device.addr))
		
		# variable to hold peripheral device
		self.peripheral = None
		
		# retry connection until connected
		while self.peripheral is None:
			try:
				self.peripheral = Peripheral(self.device, "random")
			except Exception as err:
				self.logger.exception("Caught exception: Peripheral connection failed at /usr/local/lib/python3.4/dist-packages/bluepy/btle.py")
		self.logger.info("Connected.")	
		
		# getting firmware version of parrotflower		
		device_info_service = self.peripheral.getServiceByUUID(UUID(SERVICES["DEVICE_INFO"]))
		firmware_ver_ch = device_info_service.getCharacteristics(UUID(CONTROLS["FIRMWARE_VER"]))[0]
		
		# check firmware
		self.checkFirmware(firmware_ver_ch.read())

		# getting live services and controlling led and live measure period
		self.live_service = self.peripheral.getServiceByUUID(UUID(SERVICES["LIVE"]))  
		# setting live measure period
		self.set_live_measure_period()	
	
		# getting pf battery service
		self.battery_service = self.peripheral.getServiceByUUID(UUID(SERVICES["BATTERY"]))


	def disconnect(self):
		self.peripheral.disconnect()
