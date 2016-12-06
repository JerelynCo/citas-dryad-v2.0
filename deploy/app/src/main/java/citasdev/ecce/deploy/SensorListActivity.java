package citasdev.ecce.deploy;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;

import java.util.ArrayList;

import citasdev.ecce.deploy.utils.BTComm;
import citasdev.ecce.deploy.utils.SensorDetails;

/**
 * Created by jerelynco on 12/3/16.
 */
public class SensorListActivity extends ListActivity {

    private static final String TAG = SensorListActivity.class.getSimpleName();
    private static final int RC_BARCODE_CAPTURE = 9001;

    private ArrayList<SensorDetails> _sensorItems = new ArrayList<SensorDetails>();
    private ArrayAdapter<SensorDetails> _sensorAdapter;

    DeployApplication _dpApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        TextView timestamp = (TextView) findViewById(R.id.tv_time_scanned);
        timestamp.setText(getIntent().getStringExtra("TIMESTAMP"));

        Button btnAddNode = (Button) findViewById(R.id.btn_new_node);
        btnAddNode.setVisibility(View.VISIBLE);

        // Getting data from application
        _dpApp = (DeployApplication) getApplicationContext();
        _sensorItems = _dpApp.get_sensorItems();

        _sensorAdapter = new ArrayAdapter<SensorDetails>(this, android.R.layout.simple_list_item_1, _sensorItems);
        setListAdapter(_sensorAdapter);
        ListView sensorList = (ListView) findViewById(android.R.id.list);


        sensorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SensorDetails sensorDetails = _sensorAdapter.getItem(i);
                Intent intent = new Intent(SensorListActivity.this, SensorDetailsActivity.class);
                intent.putExtra("SDObject", sensorDetails);
                startActivity(intent);
            }
        });
    }

    public void addNode(View v) {
        Intent intent = new Intent(this, QRCaptureActivity.class);
        intent.putExtra(QRCaptureActivity.AutoFocus, true);
        intent.putExtra(QRCaptureActivity.UseFlash, false);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    SensorDetails newSd = data.getParcelableExtra("SDObject");
                    BTComm btComm = new BTComm("QQRSN:id="+newSd.get_id()+",name="+newSd.get_broadcastName()
                            +",state="+newSd.get_state()+",site_name="+newSd.get_siteName()+",lat="+newSd.get_lat()
                            +",lon="+newSd.get_lon()+",updated="+newSd.get_dateUpdated()+";",
                            _dpApp.get_btDevice(), TAG);
                    Thread tComm = new Thread(btComm);
                    tComm.start();
                    try {
                        tComm.join();
                        Log.d(TAG, "Joined");
                        _dpApp.addSensorItem(newSd);
                        _sensorItems = _dpApp.get_sensorItems();
                        _sensorAdapter.notifyDataSetChanged();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }
}
