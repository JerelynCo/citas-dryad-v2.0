from bluepy.btle import Peripheral, UUID
import time
from threading import Event

## CONSTANTS ##
UUIDS = {
	"SERIAL"	: "0000dfb1-0000-1000-8000-00805f9b34fb",
	"COMMAND"	: "0000dfb2-0000-1000-8000-00805f9b34fb",
	"MODEL_NO"	: "00002a24-0000-1000-8000-00805f9b34fb",
	"NAME"		: "00002a00-0000-1000-8000-00805f9b34fb"  
}

UUID_SERIALSTR = str(bytearray(b"AT+PASSWOR=DFRobot\r\n"))
DFR_BDR_STR = str(bytearray(b"AT+CURRUART=115200\r\n"))



