package gal.usc.etse.es.tapeacompostela.exception;

// The message says what is missing, e.g. "Business 7 not found"
public class NotFoundException extends Exception {
    public NotFoundException(String resource, Object id) {
        super(resource+" "+id+" not found");
    }
}
