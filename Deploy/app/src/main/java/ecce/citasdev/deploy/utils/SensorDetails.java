package ecce.citasdev.deploy.utils;

import android.location.Location;

import java.util.Date;

/**
 * Created by jerelynco on 9/18/16.
 */
public class SensorDetails {
    private String _id = "";
    private Boolean _deployed;
    private Date _date = new Date();
    private Location _location;


    public SensorDetails(String id, boolean deployed, Location location){
        this._id = id;
        this._deployed = deployed;
        this._location = location;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public String getDeployed() {
        if(_deployed) {
            return "Deployed";
        }
        return "Pending Deployment";
    }

    public void setDeployed(Boolean deployed) {
        this._deployed = deployed;
    }

    public String getDate() {
        return _date.toString();
    }


    public Location getLocation() {
        return _location;
    }

    public void setLocation(Location location) {
        this._location = location;
    }

    public String getLatitude(){
        return String.valueOf(_location.getLatitude());
    }

    public String getLongitude(){
        return String.valueOf(_location.getLongitude());
    }

    @Override
    public String toString() {
        String info = "ID : " + _id + "; Lat: " + _location.getLatitude() + "; Lon: " + _location.getLongitude();
        return info;
    }


}
