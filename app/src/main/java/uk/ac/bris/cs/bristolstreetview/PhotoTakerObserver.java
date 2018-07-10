package uk.ac.bris.cs.bristolstreetview;

public interface PhotoTakerObserver {

    void onPhotoTaken(String url);
    void onPhotoSavedAndProcessed(String fullPath);

}
