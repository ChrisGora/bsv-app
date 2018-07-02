package uk.ac.bris.cs.bristolstreetview;

public class CameraInfo {
    private String mSerialNumber;
    private String mFirmwareVersion;
    private Boolean mIsGpsPresent;
    private Boolean mIsGyroPresent;

    public CameraInfo() {
    }

    public String getSerialNumber() {
        return mSerialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        mSerialNumber = serialNumber;
    }

    public String getFirmwareVersion() {
        return mFirmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        mFirmwareVersion = firmwareVersion;
    }

    public Boolean getGpsPresent() {
        return mIsGpsPresent;
    }

    public void setGpsPresent(Boolean gpsPresent) {
        mIsGpsPresent = gpsPresent;
    }

    public Boolean getGyroPresent() {
        return mIsGyroPresent;
    }

    public void setGyroPresent(Boolean gyroPresent) {
        mIsGyroPresent = gyroPresent;
    }
}
