package gal.usc.etse.es.tapeacompostela.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// The file is not a JPEG, PNG, WebP or GIF picture
@Getter
@RequiredArgsConstructor
public class InvalidImageException extends Exception {
    private final String contentType;
}
