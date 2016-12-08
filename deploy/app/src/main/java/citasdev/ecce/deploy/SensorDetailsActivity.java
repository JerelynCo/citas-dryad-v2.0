package citasdev.ecce.deploy;

import android.Manifest;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import citasdev.ecce.deploy.utils.BTComm;
import citasdev.ecce.deploy.utils.SensorDetails;

/**
 * Created by jerelynco on 12/3/16.
 */
public class SensorDetailsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = SensorDetailsActivity.class.getSimpleName();
    private static final int RC_LOCATION_PERM = 100;

    private EditText _etSnName, _etSiteName, _etLatitude, _etLongitude, _etState;
    private TextView _tvPfBatt, _tvBlBatt, _tvTimestamp;
    private LinearLayout _llSensorDetails;

    private SensorDetails _sd;
    private DeployApplication _dpApp;

    // location objects
    private Location _lastLocation;
    private GoogleApiClient _googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_details);

        _dpApp = (DeployApplication) getApplicationContext();

        _llSensorDetails = (LinearLayout) findViewById(R.id.sensor_details);
        _etSnName = (EditText) findViewById(R.id.et_sn_name);
        _etSiteName = (EditText) findViewById(R.id.et_site_name);
        _etLatitude = (EditText) findViewById(R.id.et_latitude);
        _etLongitude = (EditText) findViewById(R.id.et_longitude);
        _etState = (EditText) findViewById(R.id.et_state);

        _tvPfBatt = (TextView) findViewById(R.id.tv_pf_batt);
        _tvBlBatt = (TextView) findViewById(R.id.tv_bl_batt);
        _tvTimestamp = (TextView) findViewById(R.id.tv_timestamp);

        _sd = getIntent().getExtras().getParcelable("SDObject");

        _etSnName.setText(_sd.get_name());
        _etSiteName.setText(_sd.get_siteName());
        _etLatitude.setText(String.valueOf(_sd.get_lat()));
        _etLongitude.setText(String.valueOf(_sd.get_lon()));
        _etState.setText(_sd.get_state());
        _tvTimestamp.setText(_sd.get_dateUpdated());
        _tvBlBatt.setText(_sd.get_blBatt());
        _tvPfBatt.setText(_sd.get_pfBatt());

        _llSensorDetails.setVisibility(View.VISIBLE);

        _googleApiClient = _dpApp.get_googleApiClient();
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

        BTComm btComm = new BTComm("QSUPD:name=" + _etSnName.getText() +
                ",site_name=" + _etSiteName.getText() + ",state=" + _etState.getText() +
                ",lat=" + _etLatitude.getText() + ",lon=" + _etLongitude.getText() + ";",
                _dpApp.get_btDevice(), TAG);
        Thread tcomm = new Thread(btComm);
        tcomm.start();

        tcomm.join();
        _etLatitude.setText(String.valueOf(_lastLocation.getLatitude()));
        _etLongitude.setText(String.valueOf(_lastLocation.getLongitude()));

    }
    public void deleteNode(View v) throws InterruptedException {
        BTComm btComm = new BTComm("QDLTE:rpi_name=" + _dpApp.get_btDevice().getName() +
                ",sn_name=" + _sd.get_name() + ";", _dpApp.get_btDevice(), TAG);
        Thread tComm = new Thread(btComm);
        tComm.start();

        tComm.join();
        _dpApp.removeSensorItem(_sd.get_name());
        Intent intent = new Intent(SensorDetailsActivity.this, SensorListActivity.class);
        startActivity(intent);
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
