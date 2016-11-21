from bluepy.btle import Scanner
from dryad import parrot_ble
from dryad import bluno_ble
from collections import defaultdict
import logging
import threading
import time

# number of times to read data
N_READ = 3
exitFlag = 0
devices = {
	"c4:be:84:28:89:4a": {"type": "bluno", "id": "sn1", "data":{}},
	"a0:14:3d:84:1b:34": {"type": "parrot", "id": "sn1", "data":{}}
}

class Thread (threading.Thread):
	def __init__(self, device, entry):
		threading.Thread.__init__(self)
		self.device = device
		self.entry = entry
		self.name = self.entry["id"] + "_" + self.entry["type"]
		self.counter = 5
		self.sensor_type = self.entry["type"]

	def run(self):
		print_time(self.name, 5, self.counter)
		temp_counter = self.counter
		if self.sensor_type == "bluno":
			bl = bluno_ble.Bluno(self.device, self.name, N_READ)
			bl.start()
			print(bl.add_timestamp(bl.get_readings()))

		elif self.sensor_type == "parrot":
			pf = parrot_ble.Parrot(self.device, self.name, N_READ)
			pf.start()
			print(pf.add_timestamp(pf.get_agg_readings()))
		print("Exiting " + self.name)


def print_time(threadName, delay, counter):
	while counter:
		if exitFlag:
			threadName.exit()
		time.sleep(delay)
		print("{}:{}".format(threadName, time.ctime(time.time())))
		counter -= 1


def setup_log():
	# logger creation
    global logger
    logger = logging.getLogger("main")
    logger.setLevel(logging.DEBUG)

    # console handler
    ch = logging.StreamHandler()
    ch.setLevel(logging.DEBUG)

    # file handler
    fh = logging.FileHandler("cache_node.log")

    # formatter
    formatter = logging.Formatter("%(asctime)s - [%(levelname)s] [%(threadName)s] (%(module)s:%(lineno)d) %(message)s") 
 
    # ch setting
    ch.setFormatter(formatter)
    logger.addHandler(ch)   

    # fh setting
    fh.setFormatter(formatter)
    logger.addHandler(fh) 
    
def main():
	setup_log()
	logger.info("Program has started..")
	
	scanner = Scanner()
	logger.info("Scanning for devices...")
	scanned_devices = scanner.scan(10.0)
	
	threads = []
	
	for device in scanned_devices:
		logger.info("Device %s (%s), RSSI=%d dB" % (device.addr, device.addrType, device.rssi))	
	
		if device.connectable and device.addr in devices.keys():
			logger.info("Found device match...")
			curr_entry = devices[device.addr]
	
			if curr_entry["type"] == "bluno":
				logger.info("Match: BLUNO")
				thread = Thread(device, curr_entry)
				thread.start()	
			elif curr_entry["type"] == "parrot":
				logger.info("Match: PARROT FLOWER")
				thread = Thread(device, curr_entry)
				thread.start()

		elif not device.connectable and device.addr in devices.keys():
			logger.debug("Not connectable at the moment: " + str(device.addr))

main()
