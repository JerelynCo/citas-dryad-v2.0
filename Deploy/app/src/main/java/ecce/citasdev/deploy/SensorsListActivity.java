package ecce.citasdev.deploy;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import ecce.citasdev.deploy.utils.SensorDetails;

public class SensorsListActivity extends ListActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    ArrayList<SensorDetails> _sensorsList = new ArrayList<SensorDetails>();
    ArrayAdapter<SensorDetails> _sensorsAdapter;

    private static final String TAG = SensorsListActivity.class.getSimpleName();
    private static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location _lastLocation;
    private GoogleApiClient _googleApiClient;

    private TextView _tvLocation;
    private Button _btnGetLocation;

    private int _sensorId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_list);

        _tvLocation = (TextView) findViewById(R.id.tv_location);
        _btnGetLocation = (Button) findViewById(R.id.btn_getLocation);

        buildGoogleApiClient();

        // Show location button click listener
        _btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });

        _sensorsAdapter = new ArrayAdapter<SensorDetails>(this,
                android.R.layout.simple_list_item_1, _sensorsList);
        setListAdapter(_sensorsAdapter);
        ListView itemList = (ListView) findViewById(android.R.id.list);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SensorDetails details = _sensorsAdapter.getItem(i);
                Intent intent = new Intent(SensorsListActivity.this, SensorDetailsActivity.class);
                intent.putExtra("ID", details.getId());
                intent.putExtra("LATITUDE", details.getLatitude());
                intent.putExtra("LONGITUDE", details.getLongitude());
                intent.putExtra("DEPLOYED", details.getDeployed());
                intent.putExtra("DATETIME", details.getDate());
                startActivity(intent);
            }
        });


    }

    protected synchronized void buildGoogleApiClient() {
        _googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST) {
            Toast.makeText(this, "Requesting PERMISSION!!!", Toast.LENGTH_SHORT).show();
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission check!", Toast.LENGTH_SHORT).show();
            String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions,
                    PLAY_SERVICES_RESOLUTION_REQUEST);
            return;
        }

        _lastLocation = LocationServices.FusedLocationApi.getLastLocation(_googleApiClient);

        if (_lastLocation != null) {
            SensorDetails details = new SensorDetails(String.valueOf(_sensorId), true, _lastLocation);

            Toast.makeText(this, "Location Captured", Toast.LENGTH_SHORT).show();
            _tvLocation.setText(details.getLatitude() + ", " + details.getLongitude());

            _sensorId += 1;

            _sensorsList.add(details);
            _sensorsAdapter.notifyDataSetChanged();

        } else {
            _tvLocation.setText("Enable location.");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (_googleApiClient != null) {
            _googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        _tvLocation.setText("Press Get Location");
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
    public void onStop() {
        super.onStop();
        _googleApiClient.disconnect();
    }
}
