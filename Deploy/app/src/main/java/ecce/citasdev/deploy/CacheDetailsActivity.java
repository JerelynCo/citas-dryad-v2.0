package ecce.citasdev.deploy;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class CacheDetailsActivity extends AppCompatActivity {

    BluetoothDevice _cacheNode;
    String _details;
    private final String TAG = CacheDetailsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_details);



        TextView _tv_cache_id = (TextView) findViewById(R.id.tv_id);
        TextView _tv_version = (TextView) findViewById(R.id.tv_version);
        EditText _et_latitude = (EditText) findViewById(R.id.et_latitude);
        EditText _et_longitude = (EditText) findViewById(R.id.et_longitude);
        TextView _tv_state = (TextView) findViewById(R.id.tv_state);
        TextView _tv_task = (TextView) findViewById(R.id.tv_task);
        TextView _tv_batt = (TextView) findViewById(R.id.tv_batt);

        _cacheNode = getIntent().getParcelableExtra("BD_DEVICE");
        _details = getIntent().getStringExtra("RESPONSE");

        _tv_cache_id.setText(_cacheNode.getAddress().toString());

        try {
            JSONObject _jsonDetails = new JSONObject(_details);
            _tv_version.setText(_jsonDetails.getString("version"));
            _et_latitude.setText(_jsonDetails.getString("lat"));
            _et_longitude.setText(_jsonDetails.getString("lon"));
            _tv_state.setText(_jsonDetails.getString("state"));
            _tv_batt.setText(_jsonDetails.getString("batt"));


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void displaySensors(View v){
        Intent intent = new Intent(this, SensorListActivity.class);
        startActivity(intent);
    }
}
