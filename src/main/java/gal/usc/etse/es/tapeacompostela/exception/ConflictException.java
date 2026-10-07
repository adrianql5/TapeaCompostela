package gal.usc.etse.es.tapeacompostela.exception;

import lombok.Getter;

// The request clashes with the data, e.g. it already exists. location is the path of the existing one, if any
@Getter
public class ConflictException extends Exception {
    private final String location;

    public ConflictException(String message) {
        this(message, null);
    }

    public ConflictException(String message, String location) {
        super(message);
        this.location = location;
    }
}
