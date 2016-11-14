from bluepy.btle import Scanner
from dryad import parrot_ble
from dryad import bluno_ble
from collections import defaultdict
import logging

N_READ = 3

devices = {
	"c4:be:84:28:89:4a": {"type": "bluno", "id": "sn1"},
	"a0:14:3d:84:1b:34": {"type": "parrot", "id": "sn1"}
}

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
	
	for device in scanned_devices:
		logger.info("Device %s (%s), RSSI=%d dB" % (device.addr, device.addrType, device.rssi))	
	
		if device.connectable and device.addr in devices.keys():
			logger.info("Found device match...")
			curr_device = devices[device.addr]
	
			if curr_device["type"] == "bluno":
				logger.info("Match: BLUNO")
				bl = bluno_ble.Bluno(device, curr_device["id"] + "_bluno", N_READ)
				bl.start()
				curr_device["data"] = bl.get_agg_readings()
	

			elif curr_device["type"] == "parrot":
				logger.info("Match: PARROT FLOWER")
				pf = parrot_ble.Parrot(device, curr_device["id"] + "_parrot", N_READ)
				pf.start()
				curr_device["data"] = pf.get_agg_readings()
		
		elif not device.connectable and device.addr in devices.keys():
			logger.debug("Not connectable at the moment: " + str(device.addr)
)
	
	merged_data = defaultdict(dict)	
	for key, values in devices.items():
		sn_id = values["id"]
		merged_data[sn_id].update(values["data"])
		merged_data[sn_id].update({values["type"]+"_addr": key})

	logger.info("MERGED: {}".format(merged_data))	
main()
