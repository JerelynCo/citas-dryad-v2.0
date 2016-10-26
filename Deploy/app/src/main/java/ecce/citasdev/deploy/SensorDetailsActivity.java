package ecce.citasdev.deploy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;

import ecce.citasdev.deploy.utils.BTComm;
import ecce.citasdev.deploy.utils.SensorDetails;

public class SensorDetailsActivity extends AppCompatActivity {

    private static final String TAG = SensorDetailsActivity.class.getSimpleName();

    private TextView _tvID, _tvBName;
    private EditText _etLatitude, _etLongitude, _etState, _etPfBatt, _etBlBatt, _etTimestamp;
    private LinearLayout _llSensorDetails;

    SensorDetails _sd;
    DeployApplication _dpApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_details);

        _dpApp = (DeployApplication) getApplicationContext();

        _llSensorDetails = (LinearLayout) findViewById(R.id.sensor_details);
        _tvID = (TextView) findViewById(R.id.tv_id);
        _tvBName = (TextView) findViewById(R.id.tv_bname);
        _etLatitude = (EditText) findViewById(R.id.et_latitude);
        _etLongitude = (EditText) findViewById(R.id.et_longitude);
        _etState = (EditText) findViewById(R.id.et_state);
        _etPfBatt = (EditText) findViewById(R.id.et_pf_batt);
        _etBlBatt = (EditText) findViewById(R.id.et_bl_batt);
        _etTimestamp = (EditText) findViewById(R.id.et_timestamp);

        _sd = getIntent().getExtras().getParcelable("SDObject");

        _tvID.setText(_sd.get_id());
        _tvBName.setText(_sd.get_broadcastName());
        _etLatitude.setText(String.valueOf(_sd.get_lat()));
        _etLongitude.setText(String.valueOf(_sd.get_lon()));
        _etState.setText(_sd.get_state());
        _etTimestamp.setText(_sd.get_dateUpdated());
        _etBlBatt.setText(_sd.get_blBatt());
        _etPfBatt.setText(_sd.get_pfBatt());

        _llSensorDetails.setVisibility(View.VISIBLE);
    }

    public void deleteNode(View v){
        BTComm btComm = new BTComm("QDLTE:rpi_id="+_dpApp.get_btDevice().getAddress().toString()+
                ",sn_id="+_sd.get_id()+";", _dpApp.get_btDevice(), TAG);
        Thread tComm = new Thread(btComm);
        tComm.start();
        try {
            tComm.join();
            Intent intent = new Intent(SensorDetailsActivity.this, SensorListActivity.class);
            intent.putExtra("DELETE", _sd.get_id());
            intent.putExtra("SENSORS", "");
            startActivity(intent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

