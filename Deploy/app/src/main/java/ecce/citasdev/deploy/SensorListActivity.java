package ecce.citasdev.deploy;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import ecce.citasdev.deploy.utils.SensorDetails;

public class SensorListActivity extends ListActivity {

    private static final String TAG = SensorListActivity.class.getSimpleName();

    private ArrayList<SensorDetails> _sensorItems = new ArrayList<SensorDetails>();
    private ArrayAdapter<SensorDetails> _sensorAdapter;

    private String _details;
    private JSONObject _jsonDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        TextView timestamp = (TextView) findViewById(R.id.tv_time_scanned);
        timestamp.setText(getIntent().getStringExtra("TIMESTAMP"));

        _sensorAdapter = new ArrayAdapter<SensorDetails>(this, android.R.layout.simple_list_item_1, _sensorItems);
        setListAdapter(_sensorAdapter);
        ListView sensorList = (ListView) findViewById(android.R.id.list);

        _details = getIntent().getStringExtra("SENSORS");

        try {
            _jsonDetails = new JSONObject(_details);
            JSONArray jsArray = _jsonDetails.getJSONArray("sensor_id");
            JSONObject jObject;
            for(int i = 0; i < jsArray.length(); i++){
                jObject = jsArray.getJSONObject(i);
                SensorDetails sd_entry = new SensorDetails(
                        jObject.getString("id"), jObject.getString("name"),
                        jObject.getString("site_name"), jObject.getString("state"),
                        jObject.getString("lat"), jObject.getString("lon"),
                        jObject.getString("pf_batt"), jObject.getString("bl_batt"),
                        jObject.getString("date_updated"));
                _sensorItems.add(sd_entry);
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
}
