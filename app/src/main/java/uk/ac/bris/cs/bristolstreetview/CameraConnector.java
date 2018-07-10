package uk.ac.bris.cs.bristolstreetview;

interface CameraConnector {

    void registerObserver(CameraConnectorObserver observer);
    void removeObserver(CameraConnectorObserver observer);

    void updateCameraInfo();
    void updateCameraState();
    void setShutterVolume(int volume);

    void sendTakePhotoRequest();
    void requestDownloadPhotoAsBytes(String url);

}
