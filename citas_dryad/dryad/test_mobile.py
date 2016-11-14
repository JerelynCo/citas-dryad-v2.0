from mobile_bt import MobileNode

ml = MobileNode()
while not ml.connected:
	print("Listening for connections")
	ml.listen()

while ml.connected:
	print("Connected")
	data = receive_data()
	if data is not None:
		ml.send_response(data)


