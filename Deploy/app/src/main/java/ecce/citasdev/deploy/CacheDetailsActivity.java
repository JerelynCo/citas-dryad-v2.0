package ecce.citasdev.deploy;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import ecce.citasdev.deploy.utils.BTComm;

public class CacheDetailsActivity extends AppCompatActivity {


    private final String TAG = CacheDetailsActivity.class.getSimpleName();
    private BluetoothDevice _cacheNode;
    private String _details;

    private boolean _isNodeActivated = true;
    DeployApplication _dpApp;
    BTComm _btComm;

    JSONObject _jsonDetails;
    Thread _tComm;

    Button btn_activate;
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
        btn_activate = (Button) findViewById(R.id.btn_activate_node);
        _tv_state = (TextView) findViewById(R.id.tv_state);

        _cacheNode = _dpApp.get_btDevice();
        _details = getIntent().getStringExtra("RESPONSE");


        _tv_cache_id.setText(_cacheNode.getAddress().toString());

        try {
             _jsonDetails = new JSONObject(_details);

            if(_jsonDetails.getString("state").equals("inactive")){
                _isNodeActivated = false;
                btn_activate.setText("Activate Node");
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

    public void displaySensors(View v) throws InterruptedException {
        _btComm = new BTComm("QSLST", _dpApp.get_btDevice(), TAG);
        _tComm = new Thread(_btComm);
        _tComm.start();
        _tComm.join();

        Log.i(TAG, _btComm.get_sResponseMsg());

        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E MM dd, yyyy hh:mm:ss");

        Intent intent = new Intent(this, SensorListActivity.class);
        intent.putExtra("SENSORS", _btComm.get_sResponseMsg());
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
                btn_activate.setText("Activate Node");
            }
            else{
                _isNodeActivated = true;
                _tv_state.setText("Activated");
                btn_activate.setText("Deactivate Node");
            }
        }
    }
}
