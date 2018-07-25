package uk.ac.bris.cs.bristolstreetview;

public interface PhotoTakerObserver {

    void onPhotoTaken(PhotoRequest photoRequest);
    void onPhotoSavedAndProcessed(PhotoRequest photoRequest);

}
