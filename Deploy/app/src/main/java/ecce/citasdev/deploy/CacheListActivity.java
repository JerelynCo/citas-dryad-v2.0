package ecce.citasdev.deploy;


import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;


public class CacheListActivity extends ListActivity {

    private static final String TAG = CacheListActivity.class.getSimpleName();

    BluetoothSocket _socket;
    BluetoothDevice _device;

    private final byte DELIMITER = 59; // ";" byte representative
    private int readBufferPosition = 0;

    private String _response;

    private ArrayList<String> _cacheItems = new ArrayList<String>();
    private ArrayAdapter<String> _cacheAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sink_list);

        final Handler handler = new Handler();

        //transforming bd_list to string for readability
        final ArrayList<BluetoothDevice> bd_list = getIntent().getExtras().getParcelableArrayList("BD_DEVICES");
        for(BluetoothDevice bd: bd_list){
            _cacheItems.add(bd.getName() + ": " + bd.getAddress());
        }
        _cacheAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, _cacheItems);
        setListAdapter(_cacheAdapter);
        ListView cacheList = (ListView) findViewById(android.R.id.list);

        final class workerThread implements Runnable {
            private String btMsg;
            public workerThread(String msg) {
                btMsg = msg;
            }


            public void run() {
                sendBtMsg(btMsg);
                while(!Thread.currentThread().isInterrupted())
                {
                    int bytesAvailable;
                    boolean workDone = false;
                    _response = "no";

                    try {
                        final InputStream mmInputStream;
                        mmInputStream = _socket.getInputStream();
                        bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            Log.e("bt received","bytes available");
                            byte[] readBuffer = new byte[1024];
                            mmInputStream.read(packetBytes);

                            for(int i=0; i < bytesAvailable; i++)
                            {
                                byte b = packetBytes[i];
                                if(b == DELIMITER) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    _response = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    Log.i(TAG, "Responded: "+ _response);

                                    workDone = true;
                                    break;
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }

                            if (workDone == true) {
                                Log.i(TAG, "Work done. Socket closing.");
                                _socket.close();

                                Intent intent = new Intent(CacheListActivity.this, CacheDetailsActivity.class);
                                intent.putExtra("BD_DEVICE", _device);
                                intent.putExtra("RESPONSE", _response);
                                startActivity(intent);

                                break;
                            }
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        };

        cacheList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // transforming of selected cache node from string back to bluetoothdevice
                _device = null;
                String cacheAddr = _cacheAdapter.getItem(i).split(": ")[1];
                for(BluetoothDevice bd: bd_list){
                    if(String.valueOf(bd.getAddress()).equals(cacheAddr)){
                        _device = bd;
                    }
                }
                (new Thread(new workerThread("QSTAT"))).start();
            }
        });
    }


    public void sendBtMsg(String msg2send){
        UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID
        try {
            _socket = _device.createRfcommSocketToServiceRecord(uuid);
            if (!_socket.isConnected()){
                _socket.connect();
            }

            String msg = msg2send;
            OutputStream mmOutputStream = _socket.getOutputStream();
            mmOutputStream.write(msg.getBytes());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "EXEPTION!");
            e.printStackTrace();
        }
    }
}
