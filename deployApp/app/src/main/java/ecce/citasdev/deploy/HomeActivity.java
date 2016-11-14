package ecce.citasdev.deploy;

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

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final String TAG = HomeActivity.class.getSimpleName();

    private ArrayList<BluetoothDevice> _alDevicesList = new ArrayList<BluetoothDevice>();

    BluetoothAdapter mBluetoothAdapter;
    private ProgressDialog mProgressDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Device does not support bluetooth");
        }

        // Request for bluetooth permission if not enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Progress dialog when bluetooth scanning
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mBluetoothAdapter.cancelDiscovery();
            }
        });

        // Register the BroadcastReceiver and add bluetooth adapter states
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    // Methods for managing received broadcasts from nearby bluetooth-enabled devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    Toast.makeText(HomeActivity.this, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mProgressDlg.show();
                Toast.makeText(HomeActivity.this, "Discovery Started", Toast.LENGTH_SHORT).show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();
                Toast.makeText(HomeActivity.this, "Discovery finished", Toast.LENGTH_SHORT).show();

                Date date = new Date();
                SimpleDateFormat ft = new SimpleDateFormat("E MM dd, yyyy hh:mm:ss");


                Intent newIntent = new Intent(HomeActivity.this, CacheListActivity.class);
                newIntent.putParcelableArrayListExtra("BD_DEVICES", _alDevicesList);
                newIntent.putExtra("TIMESTAMP", ft.format(date).toString());
                startActivity(newIntent);

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName().toLowerCase().contains("rpi")) {
                    _alDevicesList.add(device);
                    Toast.makeText(HomeActivity.this, "Found device " + device.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public void onDestroy(){
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
        super.onPause();
    }

    // Starts scanning for bluetooth devices
    public void scanSinkNodes(View v){
        mBluetoothAdapter.startDiscovery();
    }
}
