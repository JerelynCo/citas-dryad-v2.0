package citasdev.ecce.deploy;

import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import citasdev.ecce.deploy.utils.BTComm;

/**
 * CacheListActivity lists all scanned devices for selection
 *
 * Created by jerelynco on 12/3/16.
 */
public class CacheListActivity extends ListActivity {
    private static final String TAG = CacheListActivity.class.getSimpleName();

    private ArrayList<String> _alCacheItems = new ArrayList<>();
    private ArrayAdapter<String> _cacheAdapter;
    private TextView tvTimeScanned;

    private DeployApplication _dpApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        // Setting Application
        _dpApp = (DeployApplication) getApplicationContext();

        // Setting timestamp to view
        tvTimeScanned = (TextView) findViewById(R.id.tv_time_scanned);
        tvTimeScanned.setText(getIntent().getStringExtra("TIMESTAMP"));

        // Since using common xml file with SensorListActivity, must hide "Add Sensor Node" button
        Button btnAddNode = (Button) findViewById(R.id.btn_new_node);
        btnAddNode.setVisibility(View.INVISIBLE);

        // extracting bd_list and transforming bd_list to string for readability
        final ArrayList<BluetoothDevice> bd_list = getIntent().getExtras().getParcelableArrayList("BD_LIST");
        if (bd_list != null) {
            for (BluetoothDevice bd : bd_list) {
                _alCacheItems.add(bd.getName() + ": " + bd.getAddress());
            }
        }

        // setting up list adapter
        _cacheAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, _alCacheItems);
        setListAdapter(_cacheAdapter);
        ListView cacheList = (ListView) findViewById(android.R.id.list);

        // set up click listener
        cacheList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // transforming of selected cache node from string back to bluetoothdevice
                String cacheAddr = _cacheAdapter.getItem(i).split(": ")[1];
                if (bd_list != null) {
                    for (BluetoothDevice bd : bd_list) {
                        if (String.valueOf(bd.getAddress()).equals(cacheAddr)) {
                            _dpApp.set_btDevice(bd);

                            // establishing bluetooth communication -> connect - transmit - receive
                            BTComm btComm = new BTComm("QSTAT:;\n", bd, TAG);
                            Thread tComm = new Thread(btComm);
                            tComm.start();

                            try {
                                // waiting for the thread to end
                                tComm.join();
                                Intent intent = new Intent(CacheListActivity.this, CacheDetailsActivity.class);
                                intent.putExtra("RESPONSE", btComm.get_sResponseMsg());
                                startActivity(intent);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

    }
}