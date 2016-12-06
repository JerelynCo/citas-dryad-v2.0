package citasdev.ecce.deploy;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import citasdev.ecce.deploy.utils.SensorDetails;

/**
 * Created by jerelynco on 12/3/16.
 */
public class DeployApplication extends Application {
    private BluetoothDevice _btDevice;
    private ArrayList<SensorDetails> _sensorItems = new ArrayList<SensorDetails>();

    public void set_btDevice(BluetoothDevice _btDevice) {
        this._btDevice = _btDevice;
    }
    public BluetoothDevice get_btDevice() {
        return _btDevice;
    }

    public void populateSensorItemsList(String sResponseMsg) throws JSONException {
        // parse -> _sensorItems = sResponseMsg;
        JSONObject obj = new JSONObject(sResponseMsg);
        JSONArray sd_array = obj.getJSONArray("sensor_id");
        for(int i = 0; i < sd_array.length(); i++){
            JSONObject sd_item = sd_array.getJSONObject(i);

            String id = (String) sd_item.get("id");
            String bname = (String) sd_item.get("name");
            String site_name = (String) sd_item.get("site_name");
            String state = (String) sd_item.get("state");
            String lat = (String) sd_item.get("lat");
            String lon = (String) sd_item.get("lon");

            SensorDetails sd = new SensorDetails(id, bname, site_name, state, lat, lon);
            _sensorItems.add(sd);
        }

    }

    public ArrayList<SensorDetails> get_sensorItems() {
        return _sensorItems;
    }

    public void addSensorItem(SensorDetails newSd) {
        _sensorItems.add(newSd);
    }

    public void removeSensorItem(String id) {
        _sensorItems.remove(id);
    }
}
