package uk.ac.bris.cs.bristolstreetview;

public class CameraOutput {

    private String name;
    private String state;
    private String id;
    private CameraResults results;
    private CameraError error;
    private CameraProgress progress;

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public String getId() {
        return id;
    }

    public CameraResults getResults() {
        return results;
    }

    public CameraError getError() {
        return error;
    }

    public CameraProgress getProgress() {
        return progress;
    }
}

class CameraResults {

    private String fileUrl;

    public String getFileUrl() {
        return fileUrl;
    }
}

class CameraError {

    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

class CameraProgress {

    private Double completion;

    public Double getCompletion() {
        return completion;
    }
}
