package uk.ac.bris.cs.bristolstreetview;

public interface CameraConnectorObserver {

    void onCameraInfoUpdated(CameraInfo newCameraInfo);
    void onCameraStateUpdated(CameraState newCameraState);
    void onTakePhotoError(CameraOutput output);
    void onTakePhotoInProgress(CameraOutput output);
    void onTakePhotoDone(CameraOutput output);
}
