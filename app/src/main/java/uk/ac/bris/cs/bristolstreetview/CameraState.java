package uk.ac.bris.cs.bristolstreetview;

class CameraState {

    private String fingerprint;
    private State state;

    public String getFingerprint() {
        return fingerprint;
    }

    public State getState() {
        return state;
    }
}

class State {

    private String sessionId;
    private Double batteryLevel;

    public String getSessionId() {
        return sessionId;
    }

    public Double getBatteryLevel() {
        return batteryLevel;
    }
}
