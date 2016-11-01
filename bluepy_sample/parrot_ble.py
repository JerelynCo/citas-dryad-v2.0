from bluepy.btle import DefaultDelegate, Peripheral, UUID
import struct
import datetime

## UUID Constants ##
# Services
SERVICES = {
	"LIVE"			: "39e1fa0084a811e2afba0002a5d5c51b",
	"BATTERY"		: 0x180f
}

#Characteristics
SENSORS = {
	"SUNLIGHT"		: "39e1fa0184a811e2afba0002a5d5c51b",
	"SOIL_EC"		: "39e1fa0284a811e2afba0002a5d5c51b",
	"SOIL_TEMP"		: "39e1fa0384a811e2afba0002a5d5c51b",
	"AIR_TEMP"		: "39e1fa0484a811e2afba0002a5d5c51b",
	"SOIL_MOISTURE"		: "39e1fa0584a811e2afba0002a5d5c51b",
}

CONTROLS = {
	"LIVE_MODE_PERIOD"	: "39e1fa0684a811e2afba0002a5d5c51b",   
	"LED"			: "39e1fa0784a811e2afba0002a5d5c51b",
	"LAST_MOVE_DATE"	: "39e1fa0884a811e2afba0002a5d5c51b", 
	"BATTERY_LEVEL"		: 0x2a19
}

## data value conversions
def unpack_U16(val):
	return float(struct.unpack("<H", val)[0])

def decode_float32(val):
	return struct.unpack('f', val)[0]

def conv_temp(val):
	dec_val = 0.00000003044 * pow(val, 3.0) - 0.00008038 * pow(val, 2.0) + val * 0.1149 - 30.449999999999999
	if dec_val < -10.0:
		dec_val = -10.0
	elif dec_val > 55.0:
		dec_val = 55.0
	return dec_val

def conv_ec(val):
	if val > 1771:
		return 10.0
	dec_val = (val / 1771.0) * 10.0
	return dec_val

def conv_light(val):
	dec_val = 16655.6019 * pow(val, -1.0606619)
	return dec_val

def conv_moisture(val):
	dec_val_tmp = 11.4293 + (0.0000000010698 * pow(val, 4.0) - 0.00000152538 * pow(val, 3.0) + 0.000866976 * pow(val, 2.0) - 0.169422 * val)
	dec_val = 100.0 * (0.0000045 * pow(dec_val_tmp, 3.0) - 0.00055 * pow(dec_val_tmp, 2.0) + 0.0292 * dec_val_tmp - 0.053)
	if dec_val < 0.0:
		dec_val = 0.0
	elif dec_val > 60.0:
		dec_val = 60.0
	return dec_val

## Delegate of bluePy
# TODO use this to manipulate data
class PeripheralDelegate(DefaultDelegate):
	def __init__(self):
		DefaultDelegate.__init__(self)
	def handleNotification(self, cHandle, data):
		print("Handle: {}; Data: {}". format(cHandle,data))

# Parrot class	
class Parrot():	
	def __init__(self, device, name):
		self.name = name
		self.device = device

	def write(self, char, val):
		char.write(str.encode(val))

	def read_sensors(self, live_service, battery_service):
		timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
		battery_level_ch = battery_service.getCharacteristics(UUID(CONTROLS["BATTERY_LEVEL"]))[0]
		reading = {"TIMESTAMP":timestamp, "SUNLIGHT":None, "SOIL_EC":None, "AIR_TEMP": None, "SOIL_TEMP": None, "SOIL_MOISTURE": None, "BATTERY": int(battery_level_ch.read(), 16)}
		
		for key, val in SENSORS.items():
			char = live_service.getCharacteristics(UUID(val))[0]
			
			if char.supportsRead(): 
				val = unpack_U16(char.read())
				if key == "SUNLIGHT":
					reading[key] = conv_light(val)
				elif key == "SOIL_EC":
					reading[key] = conv_ec(val)
				elif key == "AIR_TEMP":
					reading[key] = conv_temp(val)
				elif key == "SOIL_TEMP":
					reading[key] = conv_temp(val)
				elif key == "SOIL_MOISTURE":
					reading[key] = conv_moisture(val)
		return reading		

	def start(self): 
		print("Attempting to connect to {} [{}]".format(self.name, self.device.addr))
		p = None
		while p is None:
			try:
				p = Peripheral(self.device, "random")
			except:
				print("Caught exception.. Retrying connection..")
				pass

		p.setDelegate(PeripheralDelegate())	
		print("Connected..")	
		
		# getting live services and controlling led and live measure period	
		live_service = p.getServiceByUUID(UUID(SERVICES["LIVE"]))  
		led_control_ch = live_service.getCharacteristics(UUID(CONTROLS["LED"]))[0]
		live_measure_ch = live_service.getCharacteristics(UUID(CONTROLS["LIVE_MODE_PERIOD"]))[0]
		self.write(live_measure_ch, '\x01')
		
		# getting pf battery service
		battery_service = p.getServiceByUUID(UUID(SERVICES["BATTERY"]))
		
		print("Starting sensor readings...")	
		self.write(led_control_ch, '\x01')

		while True:
			print(self.read_sensors(live_service, battery_service))
		
		self.write(led_control_ch, '\x00')
