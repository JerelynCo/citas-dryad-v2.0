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

QUERY = {
	"QUNDP"		: "QUNDP;\n",
	"QDEPL"		: "QDEPL;\n",
	"QREAD"		: "QREAD;\n",
}

RESP = {
	"RDEPL_OK"		: "RDEPL:OK",
	"RDEPL_ERR"		: "RDEPL:ERR_INV_STATE",
	"RUNDP_OK"		: "RUNDP:OK",
	"RUNDP_ERR"		: "RUNDP:ERR_INV_STATE",
	"RREAD_OK"		: "RREAD:OK",
	"RDATA_OK"		: "RDATA:pH",
}

SERIAL_HDL = 37

# Security variables
DFR_PWD_STR = str(bytearray(b"AT+PASSWOR=DFRobot\r\n"))
DFR_BDR_STR = str(bytearray(b"AT+CURRUART=115200\r\n"))

readings = np.array([])
reading = None
isReadingReady, isDeviceReady, isReceived, isDataRaw = [False] * 4

class PeripheralDelegate(DefaultDelegate):
	def __init__(self, serial_ch):
		DefaultDelegate.__init__(self)
		self.serial_ch = serial_ch
		self.logger = logging.getLogger("main.bluno_ble.PeripheralDelegate")
	def handleNotification(self, cHandle, response):
		global readings, reading, isDeviceReady, isReadingReady, isReceived
		
		response = str(response)
		
		if cHandle is SERIAL_HDL:
			if RESP["RDEPL_ERR"] in response:
				isDeviceReady = False
				self.serial_ch.write(str.encode(QUERY["QUNDP"]))
				self.logger.info("Bluno: Request to undeploy")
			if RESP["RUNDP_ERR"] in response:
				isDeviceReady = False
				self.serial_ch.write(str.encode(QUERY["QDEPL"]))
				self.logger.info("Bluno: Request to deploy")
			if RESP["RUNDP_OK"] in response:
				isDeviceReady = False
				self.logger.info("Bluno: Undeployed")
			if RESP["RDEPL_OK"] in response:
				isDeviceReady = True
				self.logger.info("Bluno: Deployed")
			if RESP["RREAD_OK"] in response:
				isReadingReady = True				
			if RESP["RDATA_OK"] in response:
				isReceived = True
				reading = response.split("=")[1].split(";")[0].strip()
				if not isDataRaw:
					reading = float(reading) * 2.2570 + 2.6675 
				readings = np.append(readings, {"PH": reading})

class Bluno():
	def __init__(self, device, name, n_read=3):
		self.name = name
		self.device = device
		self.n_read = n_read
		self.serial_ch = None
		self.logger = logging.getLogger("main.parrot_ble.Parrot")

	# start deployment
	def start_deploy(self):
		self.serial_ch.write(str.encode(QUERY["QDEPL"]))

	def stop_deploy(self):
		self.serial_ch.write(str.encode(QUERY["QUNDP"]))

	def isSuccess(self):
		return self.isSuccess

	def add_details(self, data):
		data["BL_TIMESTAMP"] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
		data["BL_ADDR"] = self.device.addr 
		return data	
	
	def get_readings(self):
		return readings
	
	# TODO Ask why have RDEND? What if streaming?
	def read_ph(self, isRaw=False):
		global isDataRaw
		isDataRaw = isRaw
		while not isReadingReady: # no need to query if reading is ready
			self.serial_ch.write(str.encode(QUERY["QREAD"]))
			if self.peripheral.waitForNotifications(1.0):
				continue
			if isReadingReady:
				break
		while True: # needs to query every time to update reading
			if self.peripheral.waitForNotifications(1.0):
				continue
			if isReceived:
				break
		return self.add_details({"PH": reading})

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
