package uk.ac.bris.cs.bristolstreetview;

interface PhotoTaker {
    void registerObserver(PhotoTakerObserver observer);

    void removeObserver(PhotoTakerObserver observer);

    void updateCameraInfo();

    void updateCameraState();

    void setShutterVolume(int volume);

    void sendTakePhotoRequest();
}
