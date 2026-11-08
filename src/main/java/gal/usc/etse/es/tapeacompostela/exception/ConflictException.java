package gal.usc.etse.es.tapeacompostela.exception;

// La petición colisiona con datos que ya existen, como por ejemplo
// un tag repetido (409)
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}