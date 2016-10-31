from bluepy.btle import Scanner
from parrot_ble import Parrot
 
def main():
	scanner = Scanner()
	print("Scanning for devices...")
	scanned_devices= scanner.scan(10.0)
	
	pf_bname = ""
	isFlowerPower = False
	for device in scanned_devices:
		print("Device %s (%s), RSSI=%d dB" % (device.addr, device.addrType, device.rssi))
		for (adtype, desc, value) in device.getScanData():
			if "Flower power" in value:
				pf_bname = value
				isFlowerPower = True
		if device.connectable and isFlowerPower:
			pf = Parrot(device, pf_bname)	
			pf.start()

main()
