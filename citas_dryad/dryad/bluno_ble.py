from bluepy.btle import Peripheral, UUID, DefaultDelegate
import datetime
import traceback
import numpy as np
from collections import defaultdict 
import logging

## CONSTANTS ##
SERVICES = {
	"CTRL"		: "0000dfb000001000800000805f9b34fb",
	"DEVINFO"	: "0000180a00001000800000805f9b34fb"
}

CTRL_CHARS = {
	"SERIAL"	: "0000dfb100001000800000805f9b34fb",
	"COMMAND"	: "0000dfb200001000800000805f9b34fb"
}


DEVINFO_CHARS = {
	"MODEL_NO"	: "00002a2400001000800000805f9b34fb",
	"NAME"		: "00002a0000001000800000805f9b34fb"  
}

SERIAL_HDL = 37

# Security variables
DFR_PWD_STR = str(bytearray(b"AT+PASSWOR=DFRobot\r\n"))
DFR_BDR_STR = str(bytearray(b"AT+CURRUART=115200\r\n"))

readings = np.array([])

class PeripheralDelegate(DefaultDelegate):
	def __init__(self, serial_ch):
		DefaultDelegate.__init__(self)
		self.serial_ch = serial_ch
		self.logger = logging.getLogger("main.bluno_ble.PeripheralDelegate")
	def handleNotification(self, cHandle, data):
		data = str(data)
		global readings
		if cHandle is SERIAL_HDL:
			if "RUNDP:OK" in data:
				self.logger.info("Bluno: Undeployed")
			if "RDEPL:OK" in data:
				self.logger.info("Bluno: Deployed")
				self.serial_ch.write(str.encode("QREAD;\n"))
			if "pH" in data:
				readings = np.append(readings, {"PH": data.split("=")[1].split(";")[0].strip()})
		

class Bluno():
	def __init__(self, device, name, n_read):
		self.name = name
		self.device = device
		self.n_read = n_read
		self.logger = logging.getLogger("main.parrot_ble.Parrot")

	# start deployment
	def start_deploy(self, serial_ch):
		serial_ch.write(str.encode("QDEPL;\r\n"))

	def stop_deploy(self, serial_ch):
		serial_ch.write(str.encode("QUNDP;\r\n"))

	def isSuccess(self):
		return self.isSuccess

	def add_timestamp(self, data):
		data["BL_TIMESTAMP"] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
		return data	
	
	def get_readings(self):
		return readings

	def get_readings_mean_var(self):
		readings = self.get_readings()
		if readings.size == 0:
			return (0, 0) 
		return (np.round(readings.mean(), 4), np.round(readings.var(),4))
	
	def get_agg_readings(self):
		aggregated_data = defaultdict(int)
		data = self.get_readings()
		for entry in data:
			aggregated_data["PH"] += float(entry["PH"])
		data = {k: v / self.n_read for k, v in aggregated_data.items()} 
		return self.add_timestamp(data)		
		

	def start(self):
		self.logger.info("Attempting to connect to {} [{}]".format(self.name, self.device.addr))
		
		# variable to hold peripheral device
		p = None
		
		# retry connection until connected
		while p is None:
			try:
				p = Peripheral(self.device, "random")
			except Exception as err:
				self.logger.exception(err)	
				self.logger.exception("Caught exception: Peripheral connection failed at /usr/local/lib/python3.4/dist-packages/bluepy/btle.py")
		self.logger.info("Connected.")
		
		# declaration of ctrl service and serial characteristic
		ctrl_service = p.getServiceByUUID(UUID(SERVICES["CTRL"]))
		serial_ch = ctrl_service.getCharacteristics(UUID(CTRL_CHARS["SERIAL"]))[0]
		
		p.setDelegate(PeripheralDelegate(serial_ch))
		
		self.start_deploy(serial_ch)
	
		temp_counter = self.n_read
		temp_counter += 1 # extra 1 for notification wait		
		while temp_counter != 0:
			if p.waitForNotifications(1.0):
				continue
			temp_counter -= 1
		
		self.stop_deploy(serial_ch)
		p.disconnect()
