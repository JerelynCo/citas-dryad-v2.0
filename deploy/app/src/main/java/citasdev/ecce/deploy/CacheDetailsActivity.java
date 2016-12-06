package citasdev.ecce.deploy;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import citasdev.ecce.deploy.utils.BTComm;

/**
 * Created by jerelynco on 12/3/16.
 */
public class CacheDetailsActivity extends AppCompatActivity {


    private final String TAG = CacheDetailsActivity.class.getSimpleName();
    private BluetoothDevice _cacheNode;
    private String _details;

    private boolean _isNodeActivated = true;
    DeployApplication _dpApp;
    BTComm _btComm;

    JSONObject _jsonDetails;
    Thread _tComm;

    Button _btn_activate;
    TextView _tv_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_details);

        _dpApp = (DeployApplication) getApplicationContext();

        TextView _tv_cache_id = (TextView) findViewById(R.id.tv_id);
        TextView _tv_version = (TextView) findViewById(R.id.tv_version);
        EditText _et_latitude = (EditText) findViewById(R.id.et_latitude);
        EditText _et_longitude = (EditText) findViewById(R.id.et_longitude);
        TextView _tv_batt = (TextView) findViewById(R.id.tv_batt);
        _btn_activate = (Button) findViewById(R.id.btn_activate_node);
        _tv_state = (TextView) findViewById(R.id.tv_state);

        _cacheNode = _dpApp.get_btDevice();
        _details = getIntent().getStringExtra("RESPONSE");

        _tv_cache_id.setText(_cacheNode.getAddress().toString());

        try {
            _jsonDetails = new JSONObject(_details);

            if(_jsonDetails.getString("state").equals("inactive")){
                _isNodeActivated = false;
                _btn_activate.setText("Activate Node");
            }
            _tv_version.setText(_jsonDetails.getString("version"));
            _et_latitude.setText(_jsonDetails.getString("lat"));
            _et_longitude.setText(_jsonDetails.getString("lon"));
            _tv_state.setText(_jsonDetails.getString("state"));
            _tv_batt.setText(_jsonDetails.getString("batt"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void displaySensors(View v) throws InterruptedException, JSONException {
        _btComm = new BTComm("QSLST", _dpApp.get_btDevice(), TAG);
        _tComm = new Thread(_btComm);
        _tComm.start();
        _tComm.join();

        Log.i(TAG, _btComm.get_sResponseMsg());

        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E MM dd, yyyy hh:mm:ss");

        // parsing of response
        _dpApp.populateSensorItemsList(_btComm.get_sResponseMsg());

        Intent intent = new Intent(this, SensorListActivity.class);
        intent.putExtra("TIMESTAMP", ft.format(date).toString());
        startActivity(intent);
    }

    public void activateNode(View v) throws JSONException, InterruptedException {
        if(_isNodeActivated){
            _btComm = new BTComm("QDEAC", _dpApp.get_btDevice(), TAG);
        }
        else{
            _btComm = new BTComm("QACTV", _dpApp.get_btDevice(), TAG);
        }

        _tComm = new Thread(_btComm);
        _tComm.start();
        _tComm.join();

        Log.i(TAG, _btComm.get_sResponseMsg());

        if(_btComm.get_sResponseMsg().equals("OK")){
            if(_isNodeActivated){
                _isNodeActivated = false;
                _tv_state.setText("Deactivated");
                _btn_activate.setText("Activate Node");
            }
            else{
                _isNodeActivated = true;
                _tv_state.setText("Activated");
                _btn_activate.setText("Deactivate Node");
            }
        }
    }
}
