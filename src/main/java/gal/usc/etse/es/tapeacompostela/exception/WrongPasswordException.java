package gal.usc.etse.es.tapeacompostela.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WrongPasswordException extends Exception {
    private final String username;
}
