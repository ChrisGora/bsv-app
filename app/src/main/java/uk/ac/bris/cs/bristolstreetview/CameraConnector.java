package uk.ac.bris.cs.bristolstreetview;

import android.location.Location;

interface CameraConnector {

//    static CameraConnector instance;
//
//    static CameraConnector getInstance() {
//        return instance;
//    }

    void registerObserver(CameraConnectorObserver observer);
    void removeObserver(CameraConnectorObserver observer);

    void setUrl(String url);

    void updateCameraInfo();
    void updateCameraState();

    void setShutterVolume(int volume);

    void sendTakePhotoRequest(PhotoRequest photoRequest);

    void requestDownloadPhotoAsBytes(PhotoRequest photoRequest);

    void deleteAll();

}
