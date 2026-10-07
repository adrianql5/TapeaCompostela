package gal.usc.etse.es.tapeacompostela.exception;

public class InvalidRefreshTokenException extends Exception {
    private final String token;

    public InvalidRefreshTokenException(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
