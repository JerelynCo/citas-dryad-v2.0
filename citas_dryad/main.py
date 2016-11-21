from bluepy.btle import Scanner
from dryad import parrot_ble
from dryad import bluno_ble
from collections import defaultdict
from utils import logging

# number of times to read data
N_READ = 3

# Enable disable flags
ENABLE = "\x01"
DISABLE = "\x00"

devices = {
	"c4:be:84:28:89:4a": {"type": "bluno", "id": "sn1", "data":{}},
	"a0:14:3d:84:1b:34": {"type": "parrot", "id": "sn1", "data":{}}
}

def main():
	logger = logging.CustomLogging("main").setup()
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
				bl = bluno_ble.Bluno(device, curr_device["id"] + "_bluno")
				bl.setup_conn()
				curr_device["data"] = bl.read_ph()
				bl.disconnect()	
			elif curr_device["type"] == "parrot":
				logger.info("Match: PARROT FLOWER")
				pf = parrot_ble.Parrot(device, curr_device["id"] + "_parrot")
				pf.setup_conn()
				pf.switch_led(ENABLE)
				curr_device["data"] = pf.add_timestamp(pf.read_sensors())
				pf.switch_led(DISABLE)
				pf.disconnect()
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
