package ecce.citasdev.deploy.utils;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jerelynco on 10/5/16.
 */
public class CacheDetails {

    private String _addr = "";
    private String _siteName = "";
    private String _status;
    private String _date;
    private Location _location;

    public CacheDetails(String addr, String siteName, String status){
        this._addr = addr;
        this._siteName = siteName;
        this._status = status;
        this._date = dateAndFormatter();
    }

    public String dateAndFormatter() {
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E MM dd, yyyy hh:mm:ss");
        return ft.format(date).toString();
    }

    public String get_addr() {
        return _addr;
    }

    public void set_addr(String _addr) {
        this._addr = _addr;
    }

    public String get_siteName() {
        return _siteName;
    }

    public void set_siteName(String _siteName) {
        this._siteName = _siteName;
    }

    public String get_status() {
        return _status;
    }

    public void set_status(String _status) {
        this._status = _status;
    }

    public String get_date() {
        return _date;
    }

    public void set_date(String _date) {
        this._date = _date;
    }

    public Location get_location() {
        return _location;
    }

    public void set_location(Location _location) {
        this._location = _location;
    }

    @Override
    public String toString(){
        return _addr;
    }
}
