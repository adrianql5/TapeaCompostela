package gal.usc.etse.es.tapeacompostela.exception;

import lombok.Getter;

// Un cliente ha pasado el límite de peticiones d eun endpoint (@RateLimited). GlobalExceptionHandler la convierte en un
// 429 con la cabecera Retry-After: los segundos que tiene que esperar
@Getter
public class RateLimitExceededException extends RuntimeException {
    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds) {
        super("Too many requests. Try again in " + retryAfterSeconds + " seconds");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}