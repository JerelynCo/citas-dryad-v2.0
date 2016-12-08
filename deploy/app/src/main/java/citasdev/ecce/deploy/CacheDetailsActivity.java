package citasdev.ecce.deploy;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import citasdev.ecce.deploy.utils.BTComm;

/**
 * CacheDetailsActivity
 * Created by jerelynco on 12/3/16.
 */
public class CacheDetailsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private final String TAG = CacheDetailsActivity.class.getSimpleName();
    private static final int RC_LOCATION_PERM = 100;

    private BluetoothDevice _cacheDevice;
    private String _deviceDetails;

    private DeployApplication _dpApp;
    private BTComm _btComm;
    private Thread _tComm;

    private boolean _isNodeActivated = true;
    private JSONObject _jsonDetails;
    private Button _btn_activate;
    private TextView _tv_state;
    private EditText _et_cache_name, _et_latitude, _et_longitude;

    // location objects
    private Location _lastLocation;
    private GoogleApiClient _googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_details);

        _et_cache_name = (EditText) findViewById(R.id.et_name);
        _et_latitude = (EditText) findViewById(R.id.et_latitude);
        _et_longitude = (EditText) findViewById(R.id.et_longitude);
        _btn_activate = (Button) findViewById(R.id.btn_activate_node);
        _tv_state = (TextView) findViewById(R.id.tv_state);

        TextView tv_version = (TextView) findViewById(R.id.tv_version);
        TextView tv_batt = (TextView) findViewById(R.id.tv_batt);

        // retrieving Application
        _dpApp = (DeployApplication) getApplicationContext();
        _cacheDevice = _dpApp.get_btDevice();

        _et_cache_name.setText(_cacheDevice.getName());

        _deviceDetails = getIntent().getStringExtra("RESPONSE").split(":", 2)[1];

        Log.d(TAG, _deviceDetails);

        buildGoogleApiClient();
        _dpApp.set_googleApiClient(_googleApiClient);

        try {
            _jsonDetails = new JSONObject(_deviceDetails);

            if(_jsonDetails.getString("state").equals("inactive")){
                _isNodeActivated = false;
                _btn_activate.setText("Activate Node");
            }
            _et_latitude.setText(_jsonDetails.getString("lat"));
            _et_longitude.setText(_jsonDetails.getString("lon"));
            _tv_state.setText(_jsonDetails.getString("state"));
            tv_version.setText(_jsonDetails.getString("version"));
            tv_batt.setText(_jsonDetails.getString("batt"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        _googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private void accessLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission check!", Toast.LENGTH_SHORT).show();
            String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions,
                    RC_LOCATION_PERM);
            return;
        }

        _lastLocation = LocationServices.FusedLocationApi.getLastLocation(_googleApiClient);

        if (_lastLocation != null) {
            Toast.makeText(this, "Location Captured", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "NO Location Captured", Toast.LENGTH_SHORT).show();
        }
    }


    public void updateDetails(View v) throws InterruptedException {
        accessLocation();

        _btComm = new BTComm("QCUPD:name=" + _et_cache_name.getText() +
                ",lat="+String.valueOf(_lastLocation.getLatitude()) +
                ",lon=" + String.valueOf(_lastLocation.getLongitude()) +
                ";\n", _dpApp.get_btDevice(), TAG);
        _tComm = new Thread(_btComm);
        _tComm.start();
        _tComm.join();

        _et_latitude.setText(String.valueOf(_lastLocation.getLatitude()));
        _et_longitude.setText(String.valueOf(_lastLocation.getLongitude()));

    }

    public void displaySensors(View v) throws InterruptedException, JSONException {
        _btComm = new BTComm("QNLST:;\n", _dpApp.get_btDevice(), TAG);
        _tComm = new Thread(_btComm);
        _tComm.start();
        _tComm.join();

        Log.i(TAG, _btComm.get_sResponseMsg());

        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E MM dd, yyyy hh:mm:ss");

        // parsing of response
        _dpApp.populateSensorItemsList(_btComm.get_sResponseMsg().split(":",2)[1]);

        Intent intent = new Intent(this, SensorListActivity.class);
        intent.putExtra("TIMESTAMP", ft.format(date).toString());
        startActivity(intent);
    }

    public void activateNode(View v) throws JSONException, InterruptedException {
        if(_isNodeActivated){
            _btComm = new BTComm("QDEAC:;\n", _dpApp.get_btDevice(), TAG);
        }
        else{
            _btComm = new BTComm("QACTV:;\n", _dpApp.get_btDevice(), TAG);
        }

        _tComm = new Thread(_btComm);
        _tComm.start();
        _tComm.join();

        if(_btComm.get_sResponseMsg().contains("OK")){
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

    public void turnOff(View v) throws InterruptedException {
        accessLocation();

        _btComm = new BTComm("QPWDN;\n", _dpApp.get_btDevice(), TAG);
        _tComm = new Thread(_btComm);
        _tComm.start();
        _tComm.join();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "Location obtained.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        _googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (_googleApiClient != null) {
            _googleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        _googleApiClient.disconnect();
    }
}
