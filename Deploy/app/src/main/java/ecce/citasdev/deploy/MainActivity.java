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
import ecce.citasdev.deploy.utils.SensorDetails;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_BARCODE_CAPTURE = 9001;

    private CompoundButton _cbAutoFocus, _cbUseFlash;
    private TextView _tvStatusMessage, _tvQRValue;
    private Button _bAcceptCode;

    private TextView _tvNodeAddr;
    private EditText _etLatitude, _etLongitude, _etDeployed, _etTimestamp;
    private LinearLayout _llSensorDetails;

    SensorDetails _sd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _tvStatusMessage = (TextView) findViewById(R.id.status_message);

        _llSensorDetails = (LinearLayout) findViewById(R.id.sensor_details);
        _tvNodeAddr = (TextView) findViewById(R.id.tv_node_addr);
        _etLatitude = (EditText) findViewById(R.id.et_latitude);
        _etLongitude = (EditText) findViewById(R.id.et_longitude);
        _etDeployed = (EditText) findViewById(R.id.et_deployed);
        _etTimestamp = (EditText) findViewById(R.id.et_timestamp);

        _cbAutoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        _cbUseFlash = (CompoundButton) findViewById(R.id.use_flash);

        _bAcceptCode = (Button) findViewById(R.id.accept_QR);

        _cbAutoFocus.setChecked(true);
        _bAcceptCode.setEnabled(false);
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
                    SensorDetails _sd = data.getParcelableExtra("SDObject");
                    _tvNodeAddr.setText(_sd.get_addr());
                    _etLatitude.setText(String.valueOf(_sd.get_location().getLatitude()));
                    _etLongitude.setText(String.valueOf(_sd.get_location().getLongitude()));
                    _etDeployed.setText(_sd.get_deployed());
                    _etTimestamp.setText(_sd.get_date());

                    _llSensorDetails.setVisibility(View.VISIBLE);

                    _bAcceptCode.setEnabled(true);
                    _tvStatusMessage.setText(R.string.QR_success);
                } else {
                    _tvStatusMessage.setText(R.string.QR_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                _tvStatusMessage.setText("Invalid QR");
                _llSensorDetails.setVisibility(View.INVISIBLE);
                _bAcceptCode.setEnabled(false);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void confirmQR(View v){
        Toast.makeText(this, "Scanning for RPI", Toast.LENGTH_SHORT).show();
    }
}

