from bluepy.btle import Peripheral, UUID, DefaultDelegate
import time
from threading import Event


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

class PeripheralDelegate(DefaultDelegate):
	def __init__(self, serial_ch):
		DefaultDelegate.__init__(self)
		self.serial_ch = serial_ch
	def handleNotification(self, cHandle, data):
		data = str(data)
		if cHandle is SERIAL_HDL:
			if "RUNDP:OK" in data:
				print("Undeployed..")
			if "RDEPL:OK" in data:
				print("RDEPL:OK")
				self.serial_ch.write(str.encode("QREAD;\n"))
			if "pH" in data:
				print(data.split("=")[1].split(";")[0].strip())
		

class Bluno():
	def __init__(self, device, name):
		self.name = name
		self.device = device
	

	def printServicesCharacteristics(self):
		for service in p.getServices():
			print("Service: {}; UUID: {}".format(service, service.uuid))
			for char in service.getCharacteristics():
				print("\t {};{}".format(char, char.uuid))	

		
	# start deployment
	def start_deploy(self, serial_ch):
		serial_ch.write(str.encode("QDEPL;\r\n"))

	def stop_deploy(self, serial_ch):
		serial_ch.write(str.encode("QUNDP;\r\n"))

					
	def start(self):
		print("Attempting to connect to {} [{}]".format(self.name, self.device.addr))
		
		# variable to hold peripheral device
		p = None
		
		# retry connection until connected
		while p is None:
			try:
				p = Peripheral(self.device, "random")
			except Exception as err:
				print("Caught exception.. Retrying connection..")
				traceback.print_tb(err.__traceback__)

		print("Connected..")
		
		# declaration of ctrl service and serial characteristic
		ctrl_service = p.getServiceByUUID(UUID(SERVICES["CTRL"]))
		serial_ch = ctrl_service.getCharacteristics(UUID(CTRL_CHARS["SERIAL"]))[0]
		
		p.setDelegate(PeripheralDelegate(serial_ch))
		
		self.stop_deploy(serial_ch)
		time.sleep(500)
		self.start_deploy(serial_ch)
		
		n_read = 12	
		while n_read > 0:
			if p.waitForNotifications(1.0):
				continue
			n_read -= 1
		print("Reading finished")
		self.stop_deploy(serial_ch)
		p.disconnect()
