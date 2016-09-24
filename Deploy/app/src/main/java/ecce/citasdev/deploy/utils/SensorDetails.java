package ecce.citasdev.deploy.utils;

import android.hardware.Sensor;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jerelynco on 9/18/16.
 */
public class SensorDetails implements Parcelable {
    private String _addr = "";
    private String _siteName = "";
    private String _deployed;
    private String _date;
    private Location _location;

    public String get_addr(){
        return _addr;
    }

    public String get_site_name(){
        return _siteName;
    }

    public String get_deployed(){
        return _deployed;
    }

    public String get_date(){
        return _date;
    }

    public Location get_location(){
        return _location;
    }

    public SensorDetails(String addr, String siteName, boolean deployed, Location location){
        this._addr = addr;
        this._siteName = siteName;
        this._deployed = string_deployed(deployed);
        this._location = location;
        this._date = dateAndFormatter();
    }

    public String dateAndFormatter() {
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E MM dd, yyyy hh:mm:ss");
        return ft.format(date).toString();
    }

    public String string_deployed(boolean deployed){
        if(deployed) {
            return "Deployed";
        }
        return "Pending Deployment";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Storing the Student data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(_addr);
        parcel.writeString(_siteName);
        parcel.writeString(_deployed);
        parcel.writeString(_date);
        _location.writeToParcel(parcel, i);
    }

    private SensorDetails(Parcel in){
        _addr = in.readString();
        _siteName = in.readString();
        _deployed = in.readString();
        _date = in.readString();
        _location = Location.CREATOR.createFromParcel(in);
    }

    public static final Creator<SensorDetails> CREATOR = new Creator<SensorDetails>() {
        @Override
        public SensorDetails createFromParcel(Parcel in) {
            return new SensorDetails(in);
        }

        @Override
        public SensorDetails[] newArray(int size) {
            return new SensorDetails[size];
        }
    };
}
