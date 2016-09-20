package ecce.citasdev.deploy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class SensorDetailsActivity extends AppCompatActivity {
    private TextView _tvId;
    private EditText _etLatitude, _etLongitude, _etDeployed, _etTimestamp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_details);

        _tvId = (TextView) findViewById(R.id.tv_cacheId);
        _etLatitude = (EditText) findViewById(R.id.et_latitude);
        _etLongitude = (EditText) findViewById(R.id.et_longitude);
        _etDeployed = (EditText) findViewById(R.id.et_deployed);
        _etTimestamp = (EditText) findViewById(R.id.et_timestamp);

        Intent intent = getIntent();
        _tvId.setText(intent.getStringExtra("ID"));
        _etLatitude.setText(intent.getStringExtra("LATITUDE"));
        _etLongitude.setText(intent.getStringExtra("LONGITUDE"));
        _etDeployed.setText(intent.getStringExtra("DEPLOYED"));
        _etTimestamp.setText(intent.getStringExtra("DATETIME"));


    }
}
