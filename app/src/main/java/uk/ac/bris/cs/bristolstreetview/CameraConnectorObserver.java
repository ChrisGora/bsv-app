package uk.ac.bris.cs.bristolstreetview;

public interface CameraConnectorObserver {

    void onCameraInfoUpdated(CameraInfo newCameraInfo);
    void onCameraStateUpdated(CameraState newCameraState);
    void onTakePhotoInProgress(PhotoRequest photoRequest, CameraOutput output);
    void onTakePhotoError(PhotoRequest photoRequest, CameraOutput output);
    void onTakePhotoDone(PhotoRequest photoRequest, CameraOutput output);
    void onPhotoAsBytesDownloaded(PhotoRequest photoRequest, byte[] photo);
}
