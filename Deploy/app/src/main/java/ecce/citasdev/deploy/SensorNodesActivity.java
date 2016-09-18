package ecce.citasdev.deploy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SensorNodesActivity extends AppCompatActivity {

    private TextView tvId, tvLatitude, tvLongitude, tvDeployed, tvDateTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_nodes);

        tvId = (TextView) findViewById(R.id.tv_nodeId);
        tvLatitude = (TextView) findViewById(R.id.tv_latitude);
        tvLongitude = (TextView) findViewById(R.id.tv_longitude);
        tvDeployed = (TextView) findViewById(R.id.tv_status);
        tvDateTime = (TextView) findViewById(R.id.tv_datetime);

        Intent intent = getIntent();
        tvId.setText(intent.getStringExtra("ID"));
        tvLatitude.setText(intent.getStringExtra("LATITUDE"));
        tvLongitude.setText(intent.getStringExtra("LONGITUDE"));
        tvDeployed.setText(intent.getStringExtra("DEPLOYED"));
        tvDateTime.setText(intent.getStringExtra("DATETIME"));








    }
}
