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
reading = None
isReadingReady, isDeviceReady = [False]*2

class PeripheralDelegate(DefaultDelegate):
	def __init__(self, serial_ch):
		DefaultDelegate.__init__(self)
		self.serial_ch = serial_ch
		self.logger = logging.getLogger("main.bluno_ble.PeripheralDelegate")
	def handleNotification(self, cHandle, data):
		global readings
		global reading
		global isDeviceReady
		global isReadingReady
	
		data = str(data)
		
		if cHandle is SERIAL_HDL:
			if "RDEPL:ERR_INV_STATE" in data:
				isDeviceReady = False
				self.serial_ch.write(str.encode("QUNDP;\r\n"))
				self.logger.info("Bluno: Request to undeploy")
			if "RUNDP:ERR_INV_STATE" in data:
				isDeviceReady = False
				self.serial_ch.write(str.encode("QDEPL;\r\n"))
				self.logger.info("Bluno: Request to deploy")
			if "RUNDP:OK" in data:
				isDeviceReady = False
				self.logger.info("Bluno: Undeployed")
			if "RDEPL:OK" in data:
				isDeviceReady = True
				self.logger.info("Bluno: Deployed")
			if "RREAD:OK" in data:
				isReadingReady = True				
			if "pH" in data:
				self.logger.info("Bluno: Data Received")
				reading = data.split("=")[1].split(";")[0].strip()
				readings = np.append(readings, {"PH": data.split("=")[1].split(";")[0].strip()})

class Bluno():
	def __init__(self, device, name, n_read=3):
		self.name = name
		self.device = device
		self.n_read = n_read
		self.serial_ch = None
		self.logger = logging.getLogger("main.parrot_ble.Parrot")

	# start deployment
	def start_deploy(self):
		self.serial_ch.write(str.encode("QDEPL;\r\n"))

	def stop_deploy(self):
		self.serial_ch.write(str.encode("QUNDP;\r\n"))

	def isSuccess(self):
		return self.isSuccess

	def add_timestamp(self, data):
		data["BL_TIMESTAMP"] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
		return data	
	
	def get_readings(self):
		return readings
	
	# TODO separate to read and stream
	def read_ph(self):
		while not isReadingReady:
			self.serial_ch.write(str.encode("QREAD;\n"))
			if self.peripheral.waitForNotifications(1.0):
				continue
			if isReadingReady:
				break
		while True:
			if self.peripheral.waitForNotifications(1.0):
				print(self.add_timestamp({"PH": reading}))

	def get_agg_readings(self):
		aggregated_data = defaultdict(int)
		data = self.get_readings()
		for entry in data:
			aggregated_data["PH"] += float(entry["PH"])
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
				self.logger.exception(err)	
				self.logger.exception("Caught exception: Peripheral connection failed at /usr/local/lib/python3.4/dist-packages/bluepy/btle.py")
		
		self.logger.info("Connected.")
		
		# declaration of ctrl service and serial characteristic
		ctrl_service = self.peripheral.getServiceByUUID(UUID(SERVICES["CTRL"]))
		self.serial_ch = ctrl_service.getCharacteristics(UUID(CTRL_CHARS["SERIAL"]))[0]
		
		self.peripheral.setDelegate(PeripheralDelegate(self.serial_ch))
		# add delay to not overwhelm sending "qdepl"
		while not isDeviceReady:
			self.start_deploy()
			if self.peripheral.waitForNotifications(1.0):
				continue	
			if isDeviceReady:
				break

	def disconnect(self):
		self.stop_deploy()
		self.peripheral.disconnect()
