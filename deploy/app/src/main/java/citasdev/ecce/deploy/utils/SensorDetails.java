package citasdev.ecce.deploy.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jerelynco on 9/18/16.
 */
public class SensorDetails implements Parcelable {
    private String _name = "";
    private String _state = "";
    private String _siteName = "";
    private String _pfAddr = "";
    private String _blAddr = "";
    private String _pfBatt = "";
    private String _blBatt = "";
    private String _lat = "";
    private String _lon = "";
    private String _dateUpdated = "";

    public SensorDetails(String bname, String state, String siteName,
                         String lat, String lon, String pfAddr,
                         String blAddr, String pfBatt, String blBatt){
        this._name = bname;
        this._state = state;
        this._dateUpdated = dateAndFormatter();
        this._siteName = siteName;
        this._lat = lat;
        this._lon = lon;
        this._pfAddr = pfAddr;
        this._blAddr = blAddr;
        this._pfAddr = pfBatt;
        this._blAddr = blBatt;
    }

    public SensorDetails(String bName,  String lat, String lon,
                         String pfAddr, String blAddr){

        this._name = bName;
        this._lat = lat;
        this._lon = lon;
        this._pfAddr = pfAddr;
        this._blAddr = blAddr;
        this._dateUpdated = dateAndFormatter();
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

    public String get_pfAddr() {
        return _pfAddr;
    }

    public void set_pfAddr(String _pfAddr) {
        this._pfAddr = _pfAddr;
    }

    public String get_blAddr() {
        return _blAddr;
    }

    public void set_blAddr(String _blAddr) {
        this._blAddr = _blAddr;
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

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }


    public String dateAndFormatter() {
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
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
        parcel.writeString(_name);
        parcel.writeString(_siteName);
        parcel.writeString(_state);
        parcel.writeString(_lat);
        parcel.writeString(_lon);
        parcel.writeString(_pfBatt);
        parcel.writeString(_blBatt);
        parcel.writeString(_pfAddr);
        parcel.writeString(_blAddr);
        parcel.writeString(_dateUpdated);
    }

    private SensorDetails(Parcel in){
        _name = in.readString();
        _siteName = in.readString();
        _state = in.readString();
        _lat = in.readString();
        _lon = in.readString();
        _pfBatt = in.readString();
        _blBatt = in.readString();
        _pfAddr = in.readString();
        _blAddr = in.readString();
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
        return _name;
    }
}
