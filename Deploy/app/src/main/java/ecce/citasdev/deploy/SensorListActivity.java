package ecce.citasdev.deploy;

import android.app.ListActivity;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import ecce.citasdev.deploy.utils.CacheDetails;
import ecce.citasdev.deploy.utils.SensorDetails;

public class SensorListActivity extends ListActivity {

    private static final String TAG = SensorListActivity.class.getSimpleName();

    private ArrayList<SensorDetails> _sensorItems = new ArrayList<SensorDetails>();
    private ArrayAdapter<SensorDetails> _sensorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sink_list);

        _sensorAdapter = new ArrayAdapter<SensorDetails>(this, android.R.layout.simple_list_item_1, _sensorItems);
        setListAdapter(_sensorAdapter);
        ListView sensorList = (ListView) findViewById(android.R.id.list);

        Location dummyLoc = new Location("GPS");
        dummyLoc.setLatitude(1.23d);
        dummyLoc.setLongitude(1.23d);
        SensorDetails dummy = new SensorDetails("addr", "hello", true, dummyLoc);
        _sensorItems.add(dummy);

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
