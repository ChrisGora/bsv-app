package uk.ac.bris.cs.bristolstreetview;

// TODO: 03/07/18 Get rid of all the setters

public class CameraInfo {
    private String serialNumber;
    private String firmwareVersion;
    private String model;
    private Boolean gps;
    private Boolean gyro;

    public CameraInfo() {
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Boolean getGpsPresent() {
        return gps;
    }

    public void setGpsPresent(Boolean gpsPresent) {
        gps = gpsPresent;
    }

    public Boolean getGyroPresent() {
        return gyro;
    }

    public void setGyroPresent(Boolean gyroPresent) {
        gyro = gyroPresent;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
