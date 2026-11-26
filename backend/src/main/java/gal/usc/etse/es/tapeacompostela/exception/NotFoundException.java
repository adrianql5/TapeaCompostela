package gal.usc.etse.es.tapeacompostela.exception;

// El recurso que se pidió no existe (404)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String resource, Object id) {
        super(resource + " " + id + " not found");
    }
}