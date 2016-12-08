package citasdev.ecce.deploy;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * The MainActivity welcomes the user to the deployment application. Sets up and
 * scans bluetooth devices to be sent to the next activity -> CacheListActivity
 *
 * @author jerelynco
 * @version 1.0
 * @since 2016-09-16
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final String TAG = MainActivity.class.getSimpleName();

    private ArrayList<BluetoothDevice> _alDevicesList = new ArrayList<BluetoothDevice>();

    private ProgressDialog _progressDlg;
    private BluetoothAdapter _bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (_bluetoothAdapter == null) {
            Log.e(TAG, "Device does not support bluetooth");
        }

        // Request for bluetooth permission if not enabled
        if (!_bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Progress dialog when bluetooth scanning
        _progressDlg = new ProgressDialog(this);
        _progressDlg.setMessage("Scanning nearby Raspberry Pi..");
        _progressDlg.setCancelable(false);
        _progressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                _bluetoothAdapter.cancelDiscovery();
            }
        });

        // Register the BroadcastReceiver and add bluetooth adapter states
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(_receiver, filter); // Don't forget to unregister during onDestroy
    }

    // Methods for managing received broadcasts from nearby bluetooth-enabled devices
    private final BroadcastReceiver _receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    Toast.makeText(MainActivity.this, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                _progressDlg.show();
                Toast.makeText(MainActivity.this, "Discovery Started", Toast.LENGTH_SHORT).show();
            }

            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                _progressDlg.dismiss();
                Toast.makeText(MainActivity.this, "Discovery finished", Toast.LENGTH_SHORT).show();
                // Prepare timestamp when scan finished
                Date date = new Date();
                SimpleDateFormat ft = new SimpleDateFormat("yyyy-dd-MM hh:mm:ss");

                // Intent including the list of devices scanned and scan timestamp
                Intent newIntent = new Intent(MainActivity.this, CacheListActivity.class);
                newIntent.putParcelableArrayListExtra("BD_LIST", _alDevicesList);
                newIntent.putExtra("TIMESTAMP", ft.format(date).toString());
                startActivity(newIntent);
            }

            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add only devices which contains "RPI" in their bluetooth broadcast name
                if(device.getName().toLowerCase().contains("rpi")) {
                    Log.d(TAG, "Found: " + device.getName());
                    _alDevicesList.add(device);
                    Toast.makeText(MainActivity.this, "Found device " + device.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public void onDestroy(){
        unregisterReceiver(_receiver);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (_bluetoothAdapter != null) {
            if (_bluetoothAdapter.isDiscovering()) {
                _bluetoothAdapter.cancelDiscovery();
            }
        }
        super.onPause();
    }

    /**
     * scanSinkNodes
     * @param v view where the method is linked with
     */
    public void scanSinkNodes(View v){
        _bluetoothAdapter.startDiscovery();
    }
}
