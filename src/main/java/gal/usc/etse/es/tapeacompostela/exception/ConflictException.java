package gal.usc.etse.es.tapeacompostela.exception;

// The request clashes with the data, e.g. it already exists. location is the path of the existing one, if any
public class ConflictException extends Exception {
    private final String location;

    public ConflictException(String message) {
        this(message, null);
    }

    public ConflictException(String message, String location) {
        super(message);
        this.location = location;
    }

    public String getLocation() {
        return location;
    }
}
