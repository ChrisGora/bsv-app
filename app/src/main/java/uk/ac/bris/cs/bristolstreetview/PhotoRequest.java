package uk.ac.bris.cs.bristolstreetview;

import android.location.Location;

import org.joda.time.DateTime;

public class PhotoRequest {

    private Location mLocation;
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
