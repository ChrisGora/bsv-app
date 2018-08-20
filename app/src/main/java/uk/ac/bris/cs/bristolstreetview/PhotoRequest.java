package uk.ac.bris.cs.bristolstreetview;

import android.location.Location;

import org.joda.time.DateTime;

public class PhotoRequest {

    private Location mLocation;

    private double locationAccuracy;
    private double bearing;
    private double bearingAccuracy;

    private DateTime mTime;

    private String mCameraUrl;
    private String mDevicePath;

    public PhotoRequest() {
        mTime = new DateTime();
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public double getLocationAccuracy() {
        return locationAccuracy;
    }

    public void setLocationAccuracy(double locationAccuracy) {
        this.locationAccuracy = locationAccuracy;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public double getBearingAccuracy() {
        return bearingAccuracy;
    }

    public void setBearingAccuracy(double bearingAccuracy) {
        this.bearingAccuracy = bearingAccuracy;
    }

    public DateTime getTime() {
        return mTime;
    }

    public String getCameraUrl() {
        return mCameraUrl;
    }

    public void setCameraUrl(String cameraUrl) {
        mCameraUrl = cameraUrl;
    }

    public String getDevicePath() {
        return mDevicePath;
    }

    public void setDevicePath(String devicePath) {
        mDevicePath = devicePath;
    }
}
