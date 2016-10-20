package ecce.citasdev.deploy;

import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import ecce.citasdev.deploy.utils.BTComm;


public class CacheListActivity extends ListActivity {

    private static final String TAG = CacheListActivity.class.getSimpleName();

    private ArrayList<String> _alCacheItems = new ArrayList<>();
    private ArrayAdapter<String> _cacheAdapter;

    private TextView tvTimeScanned ;

    DeployApplication _dpApp;
    Thread _tComm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        tvTimeScanned = (TextView) findViewById(R.id.tv_time_scanned);

        _dpApp = (DeployApplication) getApplicationContext();

        tvTimeScanned.setText(getIntent().getStringExtra("TIMESTAMP"));

        // transforming bd_list to string for readability
        final ArrayList<BluetoothDevice> bd_list = getIntent().getExtras().getParcelableArrayList("BD_DEVICES");
        if(bd_list != null) {
            for (BluetoothDevice bd : bd_list) {
                _alCacheItems.add(bd.getName() + ": " + bd.getAddress());
            }
        }

        // setting up list adapter
        _cacheAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, _alCacheItems);
        setListAdapter(_cacheAdapter);
        ListView cacheList = (ListView) findViewById(android.R.id.list);

        cacheList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // transforming of selected cache node from string back to bluetoothdevice
                String cacheAddr = _cacheAdapter.getItem(i).split(": ")[1];
                for(BluetoothDevice bd: bd_list){
                    if(String.valueOf(bd.getAddress()).equals(cacheAddr)){
                        _dpApp.set_btDevice(bd);

                        BTComm _btComm = new BTComm("QSTAT", bd, TAG);
                        _tComm = new Thread(_btComm);
                        _tComm.start();

                        try {
                            // waiting for the thread to end
                            _tComm.join();
                            Intent intent = new Intent(CacheListActivity.this, CacheDetailsActivity.class);
                            intent.putExtra("RESPONSE", _btComm.get_sResponseMsg());
                            startActivity(intent);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
