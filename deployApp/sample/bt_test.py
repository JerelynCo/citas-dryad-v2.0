import time
from bluetooth import *


server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)
port = server_sock.getsockname()[1]
uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

advertise_service( server_sock, "AquaPiServer",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ], 
#                  protocols = [ OBEX_UUID ] 
                   )

s_cache_details = "{'state':'active','batt':'100','version':'1.0','lat':'10.12','lon':'122.11'};"

s_sensors = "{'sensor_id': [{'id': 'xx-123', 'name': 'sn1', 'state': 'pending deployment', 'date_updated': '12-10-94', 'site_name': 'Maribulan', 'lat': '10.12', 'lon': '123.12', 'pf_batt': '98', 'bl_batt': '80'}, {'id': 'xx-124', 'name': 'sn2', 'state': 'deployed', 'date_updated': '12-10-95', 'site_name': 'Maribulan', 'lat': '10.12', 'lon': '125.12', 'pf_batt': '70', 'bl_batt': '80'}]};"

while True:          
	print "Waiting for connection on RFCOMM channel %d" % port

	client_sock, client_info = server_sock.accept()
	print "Accepted connection from ", client_info

	try:
		data = client_sock.recv(1024)
    		if len(data) == 0: break
        	print "received [%s]" % data
		if data == "QSTAT":
			client_sock.send(s_cache_details)
		if data == "QSLST":
			client_sock.send(s_sensors)	
		if data == "QACTV":
			client_sock.send("OK;")
		if data == "QDEAC":
			client_sock.send("OK;")
		print "sending data"
	except IOError:
		pass

	except KeyboardInterrupt:

		print "disconnected"
		client_sock.close()
		server_sock.close()
		print "all done"
		break
