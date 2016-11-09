from bluepy.btle import Scanner
from parrot_ble import Parrot
from bluno_ble import Bluno

def main():

	scanner = Scanner()
	print("Scanning for devices...")
	scanned_devices= scanner.scan(10.0)
	
	for device in scanned_devices:
		pf_bname = ""
		bl_bname = ""
	
		print("Device %s (%s), RSSI=%d dB" % (device.addr, device.addrType, device.rssi))
		for (adtype, desc, value) in device.getScanData():
			#print("adtype: {}; desc: {}; value: {}".format(adtype, desc, value))
			if device.connectable:
				if "Flower power" in value:
					print("PF match..")	
					pf = Parrot(device, value)
					#pf.start()
	
				elif "sk-bcd" in value:
					print("Bluno match..")
					bl = Bluno(device, value) 
					bl.start()
			
				

main()
