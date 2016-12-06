package citasdev.ecce.deploy.utils;

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
    private String _pfBatt = "";
    private String _blBatt = "";
    private String _lat;
    private String _lon;
    private String _dateUpdated;

    public SensorDetails(String id, String bname, String siteName, String state, String lat, String lon){
        this._id = id;
        this._broadcastName = bname;
        this._siteName = siteName;
        this._state = state;
        this._dateUpdated = dateAndFormatter();
        this._lat = lat;
        this._lon = lon;
    }

    public SensorDetails(String id, String bName, String siteName, String state, String lat, String lon,
                         String pfBat, String blBat, String dateUpdated){
        this._id = id;
        this._broadcastName = bName;
        this._siteName = siteName;
        this._state = state;
        this._lat = lat;
        this._lon = lon;
        this._pfBatt = pfBat;
        this._blBatt = blBat;
        this._dateUpdated = dateUpdated;
    }

    public String get_dateUpdated() {
        return _dateUpdated;
    }

    public void set_dateUpdated(String _dateUpdated) {
        this._dateUpdated = _dateUpdated;
    }


    public String get_lat() {
        return _lat;
    }

    public void set_lat(String _lat) {
        this._lat = _lat;
    }

    public String get_lon() {
        return _lon;
    }

    public void set_lon(String _lon) {
        this._lon = _lon;
    }


    public String get_blBatt() {
        return _blBatt;
    }

    public void set_blBatt(String _blBatt) {
        this._blBatt = _blBatt;
    }

    public String get_pfBatt() {
        return _pfBatt;
    }

    public void set_pfBatt(String _pfBatt) {
        this._pfBatt = _pfBatt;
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
        parcel.writeString(_broadcastName);
        parcel.writeString(_siteName);
        parcel.writeString(_state);
        parcel.writeString(_lat);
        parcel.writeString(_lon);
        parcel.writeString(_pfBatt);
        parcel.writeString(_blBatt);
        parcel.writeString(_dateUpdated);

    }

    private SensorDetails(Parcel in){
        _id = in.readString();
        _broadcastName = in.readString();
        _siteName = in.readString();
        _state = in.readString();
        _lat = in.readString();
        _lon = in.readString();
        _pfBatt = in.readString();
        _blBatt = in.readString();
        _dateUpdated = in.readString();
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
