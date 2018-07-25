package uk.ac.bris.cs.bristolstreetview;

import android.location.Location;

interface CameraConnector {

    void registerObserver(CameraConnectorObserver observer);
    void removeObserver(CameraConnectorObserver observer);

    void updateCameraInfo();
    void updateCameraState();

    void setShutterVolume(int volume);

    void sendTakePhotoRequest(PhotoRequest photoRequest);

    void requestDownloadPhotoAsBytes(PhotoRequest photoRequest);

}
