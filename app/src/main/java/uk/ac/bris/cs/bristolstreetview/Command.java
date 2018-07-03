package uk.ac.bris.cs.bristolstreetview;

public class Command {

    private String name;
    private Parameters parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }
}

class Parameters {

    private Options options;

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }
}

class Options {

    private int _shutterVolume;

    public int get_shutterVolume() {
        return _shutterVolume;
    }

    public void set_shutterVolume(int _shutterVolume) {
        this._shutterVolume = _shutterVolume;
    }
}
