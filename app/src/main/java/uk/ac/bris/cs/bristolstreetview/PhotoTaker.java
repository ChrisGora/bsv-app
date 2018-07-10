package uk.ac.bris.cs.bristolstreetview;

public class PhotoTaker {
    private static final PhotoTaker ourInstance = new PhotoTaker();

    public static PhotoTaker getInstance() {
        return ourInstance;
    }

    private PhotoTaker() {
    }
}
