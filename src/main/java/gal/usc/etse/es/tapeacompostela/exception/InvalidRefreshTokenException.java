package gal.usc.etse.es.tapeacompostela.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InvalidRefreshTokenException extends Exception {
    private final String token;
}
