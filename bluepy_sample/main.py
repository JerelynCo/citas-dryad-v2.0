from bluepy.btle import Scanner
from parrot_ble import Parrot
from bluno_ble import Bluno


devices = {
	"c4:be:84:28:89:4a": {"type": "bluno", "id": "sn1" },
	"a0:14:3d:84:1b:34": {"type": "parrot", "id": "sn1" }
}

def main():
	scanner = Scanner()
	print("Scanning for devices...")
	scanned_devices = scanner.scan(10.0)
	
	for device in scanned_devices:
		print("Device %s (%s), RSSI=%d dB" % (device.addr, device.addrType, device.rssi))	
		if device.connectable and device.addr in devices.keys():
			print("Found device match...")
			curr_device = devices[device.addr]
			if curr_device["type"] == "bluno":
				print("A bluno device...")
				bl = Bluno(device, curr_device["id"] + "_bluno")
				bl.start()
				curr_device["data"] = bl.get_readings()
			elif curr_device["type"] == "parrot":
				print("A parrot flower device...")
				pf = Parrot(device, curr_device["id"] + "_parrot")
				pf.start()
				curr_device["data"] = pf.get_readings()
		elif not device.connectable and device.addr in devices.keys():
			print("Not connectable at the moment: " + str(device.addr)
)		
	
	for key, values in devices.items():
		print("key:{}; values:{}".format(key, values))	

main()
