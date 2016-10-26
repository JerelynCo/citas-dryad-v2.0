package ecce.citasdev.deploy;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import ecce.citasdev.deploy.utils.BTComm;
import ecce.citasdev.deploy.utils.SensorDetails;

public class SensorListActivity extends ListActivity {

    private static final String TAG = SensorListActivity.class.getSimpleName();
    private static final int RC_BARCODE_CAPTURE = 9001;

    private ArrayList<SensorDetails> _sensorItems = new ArrayList<SensorDetails>();
    private ArrayAdapter<SensorDetails> _sensorAdapter;

    private String _details;
    private JSONObject _jsonDetails;

    DeployApplication _dpApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        TextView timestamp = (TextView) findViewById(R.id.tv_time_scanned);
        timestamp.setText(getIntent().getStringExtra("TIMESTAMP"));

        Button btnAddNode = (Button) findViewById(R.id.btn_new_node);
        btnAddNode.setVisibility(View.VISIBLE);

        _dpApp = (DeployApplication) getApplicationContext();

        _sensorAdapter = new ArrayAdapter<SensorDetails>(this, android.R.layout.simple_list_item_1, _sensorItems);
        setListAdapter(_sensorAdapter);
        ListView sensorList = (ListView) findViewById(android.R.id.list);

        try {
            //TODO put details in application context
            _details = getIntent().getStringExtra("SENSORS");
            if(!_details.equals("")) {
                _jsonDetails = new JSONObject(_details);
                JSONArray jsArray = _jsonDetails.getJSONArray("sensor_id");
                addNewSensorItem(jsArray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, _details);

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

    private void addNewSensorItem(JSONArray jsArray) throws JSONException {
        JSONObject jObject;
        for (int i = 0; i < jsArray.length(); i++) {
            jObject = jsArray.getJSONObject(i);
            SensorDetails sd_entry = new SensorDetails(
                    jObject.getString("id"), jObject.getString("name"),
                    jObject.getString("site_name"), jObject.getString("state"),
                    jObject.getString("lat"), jObject.getString("lon"),
                    jObject.getString("pf_batt"), jObject.getString("bl_batt"),
                    jObject.getString("date_updated"));
            _sensorItems.add(sd_entry);
        }
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
                        _sensorItems.add(newSd);
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
