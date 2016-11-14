from bluetooth import *
import time
import logging

s_cache_details = "{'state':'active','batt':'100','version':'1.0','lat':'10.12','lon':'122.11'};"

s_sensors = "{'sensor_id': [{'id': 'xx-123', 'name': 'sn1', 'state': 'pending deployment', 'date_updated': '12-10-94', 'site_name': 'Maribulan', 'lat': '10.12', 'lon': '123.12', 'pf_batt': '98', 'bl_batt': '80'}, {'id': 'xx-124', 'name': 'sn2', 'state': 'deployed', 'date_updated': '12-10-95', 'site_name': 'Maribulan', 'lat': '10.12', 'lon': '125.12', 'pf_batt': '70', 'bl_batt': '80'}]};"

#uuid="94f39d29-7d6d-437d-973b-fba39e49d4ee"

class MobileNode():
	def __init__(self):
		self.server_sock = BluetoothSocket(RFCOMM)
		self.port = self.server_sock.getsockname()[1]
		self.client_sock = None
		self.connected = False
		self.logger = logging.getLogger("main.mobile_bt.MobileNode")

	def init_socket(self, timeout=180.0):
		# Bind socket to a port and listen	
		self.server_sock.bind(("",PORT_ANY))
		
		# Configure the socket to listening mode
		self.server_sock.listen(1)

		# Configure timeout for incoming connections (default: 180 secs)
		self.server_sock.settimeout(timeout)
		return true

	def is_connected(self):
		return self.connected
	
	def listen(self):
		print("Awaiting connections...")
		try:
			self.client_sock, client_info = self.server_sock.accept()
		except BluetoothError:
			print("No connections found")
			return False

		if client_info == None:
			print("No connections found")
			return False

		print("Connection accepted from " + str(client_info))
		self.connected = True
		return True

	def receive_data(self):
		if not self.connected:
			print("Not connected")
			return False

		if self.client_sock == None:
			print("No clients to receive data from")
			return None

		try:
			data = self.client_sock.recv(2056)
		except BluetoothError:
			self.connected = False
			return None

		if data == None:
			return None
	
		print("Data received [%s]" % data)
		self.handle_data(data)
		return data
	

	def send_response(self, resp_data):        
		if not self.connected:
			print("Not connected")
			return False

		if self.client_sock == None:
			print("No clients to respond to")
			return False

		print("Sending response...")
		try:
			self.client_sock.send(resp_data)
		except BluetoothError:
			self.connected = False
			return False

		print("RESPONSE [%s]" % resp_data)
		return True

	def handle_data(self, rcvd_data):
		if rcvd_data == "QSTAT":
			self.send_response(s_cache_details)
		if rcvd_data == "QSLST":
			self.send_response(s_sensors)	
		if rcvd_data == "QACTV":
			self.send_response("OK;")
		if rcvd_data == "QDEAC":
			self.send_response("OK;")
	
	def disconnect(self):
		if self.client_sock == None:
			print("No clients to disconnect from")
			return False

		self.client_sock.close()
		self.connected = False
		return True

	def destroy(self):
		self.server_sock.close()
		self.connected = False
		return
