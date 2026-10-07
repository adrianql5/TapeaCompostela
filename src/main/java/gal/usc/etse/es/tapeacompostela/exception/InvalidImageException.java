package gal.usc.etse.es.tapeacompostela.exception;

// The file is not a JPEG, PNG, WebP or GIF picture
public class InvalidImageException extends Exception {
    private final String contentType;

    public InvalidImageException(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
