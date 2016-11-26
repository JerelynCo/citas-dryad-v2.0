#updating & upgrading aptitude repositories
echo "Updating repositories"
sudo apt-get update && sudo apt-get -y upgrade

# retrieving and installing bluez
echo "Retrieving and installation of bluez"
wget http://www.kernel.org/pub/linux/bluetooth/bluez-5.41.tar.xz
tar xvf bluez-5.41.tar.xz

cd bluez-5.41
sudo apt-get install -y libusb-dev libdbus-1-dev libglib2.0-dev libudev-dev libical-dev libreadline-dev
./configure

make
sudo make install
cd ..

# Enabling bluez service to automatically start upon system boot
echo "Enabling bluetooth service"
sudo systemctl enable bluetooth

# adding --experimental in /lib/systemd/system/bluetooth.service ExecStart for ble
#ExecStart=/usr/lib/bluetooth/bluetoothd -C
#ExecStartPost=/usr/bin/sdptool add SP

echo "Configuring bluetooth service for ble"
sudo sed -i -e 's/bluetooth\/bluetoothd/bluetooth\/bluetoothd -C \nExecStartPost=/usr/bin/sdptool add SP' /lib/systemd/system/bluetooth.service

echo "Reloading of daemon, restarting bluetooth service, and resetting of hci0"
sudo systemctl daemon-reload
sudo systemctl restart bluetooth
sudo hciconfig hci0 down
sudo hciconfig hci0 up

# install sqlite3
echo "Installing sqlite3"
sudo apt-get install sqlite3

# installation of required python modules
pip3 install -r requirements.txt

echo "Missing: Append to sudo crontab -e.."
"""
# periodical shutdowns
0 9     * * *   root    shutdown -h now
0 13    * * *   root    shutdown -h now
0 16    * * *   root    shutdown -h now

# upon reboot
@reboot  /home/pi/WSNApp/citas_dryad/run.sh &
"""

