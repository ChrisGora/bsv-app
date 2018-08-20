package uk.ac.bris.cs.bristolstreetview;

public class ExtraPhotoInfo {

    private String id;
    private double locationAccuracy;
    private double bearing;
    private double bearingAccuracy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
