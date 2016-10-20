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

public class SensorDetailsActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = SensorDetailsActivity.class.getSimpleName();
    private static final int RC_BARCODE_CAPTURE = 9001;

    private CompoundButton _cbAutoFocus, _cbUseFlash;
    private TextView _tvStatusMessage, _tvQRValue;
    private Button _bSendToCache;

    private TextView _tvID, _tvBName;
    private EditText _etLatitude, _etLongitude, _etState, _etPfBatt, _etBlBatt, _etTimestamp;
    private LinearLayout _llSensorDetails;

    SensorDetails _sd;
    DeployApplication _dpApp;
    Thread _tComm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_details);

        _dpApp = (DeployApplication) getApplicationContext();

        _tvStatusMessage = (TextView) findViewById(R.id.status_message);
        _llSensorDetails = (LinearLayout) findViewById(R.id.sensor_details);
        _tvID = (TextView) findViewById(R.id.tv_id);
        _tvBName = (TextView) findViewById(R.id.tv_bname);
        _etLatitude = (EditText) findViewById(R.id.et_latitude);
        _etLongitude = (EditText) findViewById(R.id.et_longitude);
        _etState = (EditText) findViewById(R.id.et_state);
        _etPfBatt = (EditText) findViewById(R.id.et_pf_batt);
        _etBlBatt = (EditText) findViewById(R.id.et_bl_batt);
        _etTimestamp = (EditText) findViewById(R.id.et_timestamp);
        _cbAutoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        _cbUseFlash = (CompoundButton) findViewById(R.id.use_flash);
        _bSendToCache = (Button) findViewById(R.id.send_to_cache);

        _cbAutoFocus.setChecked(true);

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

        findViewById(R.id.read_QR).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.read_QR) {
            // launch barcode activity.
            Intent intent = new Intent(this, QRCaptureActivity.class);
            intent.putExtra(QRCaptureActivity.AutoFocus, _cbAutoFocus.isChecked());
            intent.putExtra(QRCaptureActivity.UseFlash, _cbUseFlash.isChecked());

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    _sd = data.getParcelableExtra("SDObject");
                    _tvID.setText(_sd.get_id());

                    _etLatitude.setText(String.valueOf(_sd.get_lat()));
                    _etLongitude.setText(String.valueOf(_sd.get_lon()));
                    _etState.setText(_sd.get_state());
                    _etTimestamp.setText(_sd.get_dateUpdated());

                    _etPfBatt.setText("To be set");
                    _etBlBatt.setText("To be set");

                    _llSensorDetails.setVisibility(View.VISIBLE);

                    _bSendToCache.setEnabled(true);
                    _tvStatusMessage.setText(R.string.QR_success);


                } else {
                    _tvStatusMessage.setText(R.string.QR_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                _tvStatusMessage.setText("Invalid QR");
                _llSensorDetails.setVisibility(View.INVISIBLE);
                _bSendToCache.setEnabled(false);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void sendToCache(View v){
        Toast.makeText(this, "Sending data to cache..", Toast.LENGTH_SHORT).show();
        BTComm btComm = new BTComm("QQRSN:id="+_sd.get_id()+",name="+_sd.get_broadcastName()
                +",state="+_sd.get_state()+",site_name="+_sd.get_siteName()+",lat="+_sd.get_lat()
                +",lon="+_sd.get_lon()+",updated="+_sd.get_dateUpdated()+";",
                _dpApp.get_btDevice(), TAG);
        _tComm = new Thread(btComm);
        _tComm.start();
        try {
            _tComm.join();
            Intent intent = new Intent(SensorDetailsActivity.this, CacheListActivity.class);
            startActivity(intent);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

