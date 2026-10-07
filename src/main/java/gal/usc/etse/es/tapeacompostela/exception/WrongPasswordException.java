package gal.usc.etse.es.tapeacompostela.exception;

public class WrongPasswordException extends Exception {
    private final String username;

    public WrongPasswordException(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
