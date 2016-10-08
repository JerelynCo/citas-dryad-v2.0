package ecce.citasdev.deploy.utils;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jerelynco on 9/18/16.
 */
public class SensorDetails implements Parcelable {
    private String _id = "";
    private String _broadcastName = "";
    private String _state;
    private String _siteName = "";
    private String _pfBat = "";
    private String _blBat = "";
    private Location _location;
    private String _dateUpdated;

    public SensorDetails(String addr, String siteName, boolean deployed){
        this._id = addr;
        this._siteName = siteName;
        this._state = string_deployed(deployed);
        this._dateUpdated = dateAndFormatter();
    }

    public SensorDetails(String addr, String siteName, boolean deployed, Location location){
        this._id = addr;
        this._siteName = siteName;
        this._state = string_deployed(deployed);
        this._location = location;
        this._dateUpdated = dateAndFormatter();
    }

    public String get_dateUpdated() {
        return _dateUpdated;
    }

    public void set_dateUpdated(String _dateUpdated) {
        this._dateUpdated = _dateUpdated;
    }

    public Location get_location() {
        return _location;
    }

    public void set_location(Location _location) {
        this._location = _location;
    }

    public String get_blBat() {
        return _blBat;
    }

    public void set_blBat(String _blBat) {
        this._blBat = _blBat;
    }

    public String get_pfBat() {
        return _pfBat;
    }

    public void set_pfBat(String _pfBat) {
        this._pfBat = _pfBat;
    }

    public String get_siteName() {
        return _siteName;
    }

    public void set_siteName(String _siteName) {
        this._siteName = _siteName;
    }

    public String get_state() {
        return _state;
    }

    public void set_state(String _state) {
        this._state = _state;
    }

    public String get_broadcastName() {
        return _broadcastName;
    }

    public void set_broadcastName(String _broadcastName) {
        this._broadcastName = _broadcastName;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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
        parcel.writeString(_id);
        parcel.writeString(_siteName);
        parcel.writeString(_state);
        parcel.writeString(_dateUpdated);
        _location.writeToParcel(parcel, i);
    }

    private SensorDetails(Parcel in){
        _id = in.readString();
        _siteName = in.readString();
        _state = in.readString();
        _dateUpdated = in.readString();
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

    @Override
    public String toString(){
        return _id;
    }
}
