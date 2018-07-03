package uk.ac.bris.cs.bristolstreetview;

public class CameraOutput {
    private String name;
    private String state;
    private String id;
    private CameraResults results;
    private CameraError error;
    private CameraProgress progress;

}

class CameraResults {
    private String fileUrl;
}

class CameraError {
    private String code;
    private String message;

}

class CameraProgress {

}
