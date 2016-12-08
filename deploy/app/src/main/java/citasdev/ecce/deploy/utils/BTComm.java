package citasdev.ecce.deploy.utils;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by jerelynco on 10/14/16.
 */

public class BTComm implements Runnable {
    private String _TAG = "";
    private final byte DELIMITER = 59; // ";" byte representative
//    private static final String TAG =
    private int readBufferPosition = 0;

    private String _sTransmitMsg;
    private String _sResponseMsg;
    private boolean _bWorkDone = false;

    private BluetoothSocket _btSocket;
    private BluetoothDevice _btDevice;

    public BTComm(String transmitMsg, BluetoothDevice bd, String tag) {
        this._sTransmitMsg = transmitMsg;
        this._btDevice = bd;
        this._TAG = tag;
    }

    public String get_TAG() {
        return _TAG;
    }

    public void set_TAG(String _TAG) {
        this._TAG = _TAG;
    }

    public String get_sTransmitMsg() {
        return _sTransmitMsg;
    }

    public void set_sTransmitMsg(String _sTransmitMsg) {
        this._sTransmitMsg = _sTransmitMsg;
    }

    public BluetoothDevice get_btDevice() {
        return _btDevice;
    }

    public void set_btDevice(BluetoothDevice _btDevice) {
        this._btDevice = _btDevice;
    }


    public BluetoothSocket get_btSocket() {
        return _btSocket;
    }

    public void set_btSocket(BluetoothSocket _btSocket) {
        this._btSocket = _btSocket;
    }

    public boolean is_bWorkDone() {
        return _bWorkDone;
    }

    public void set_bWorkDone(boolean _bWorkDone) {
        this._bWorkDone = _bWorkDone;
    }

    public String get_sResponseMsg() {
        return _sResponseMsg;
    }

    public void set_sResponseMsg(String _sResponseMsg) {
        this._sResponseMsg = _sResponseMsg;
    }

    @Override
    public void run() {
        sendBtMsg(_sTransmitMsg);

        while(!Thread.currentThread().isInterrupted())
        {
            int bytesAvailable;

            _sResponseMsg = "No data received";

            try {
                final InputStream mmInputStream;
                mmInputStream = _btSocket.getInputStream();
                bytesAvailable = mmInputStream.available();
                if(bytesAvailable > 0)
                {
                    byte[] packetBytes = new byte[bytesAvailable];
                    Log.i(_TAG, "Response available");
                    byte[] readBuffer = new byte[1024];
                    mmInputStream.read(packetBytes);

                    for(int i=0; i < bytesAvailable; i++)
                    {
                        byte b = packetBytes[i];
                        if(b == DELIMITER) {
                            byte[] encodedBytes = new byte[readBufferPosition];
                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                            _sResponseMsg = new String(encodedBytes, "US-ASCII");
                            readBufferPosition = 0;

                            Log.i(_TAG, "Responded: "+ _sResponseMsg);

                            _bWorkDone = true;
                            break;
                        }
                        else {
                            readBuffer[readBufferPosition++] = b;
                        }
                    }

                    if (_bWorkDone == true) {
                        Log.i(_TAG, "Work done. Socket closing.");
                        _btSocket.close();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendBtMsg(String msgToSend){
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        try {
            _btSocket = _btDevice.createRfcommSocketToServiceRecord(uuid);
            if (!_btSocket.isConnected()){
                _btSocket.connect();
            }

            String msg = msgToSend;
            OutputStream mmOutputStream = _btSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());

        } catch (IOException e) {
            Log.e(_TAG, "Exception in sending message");
            e.printStackTrace();
        }
    }
}
