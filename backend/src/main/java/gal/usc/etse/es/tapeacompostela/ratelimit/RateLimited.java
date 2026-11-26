package gal.usc.etse.es.tapeacompostela.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;


// Marca un endpoint con su límite de peticiones: capacity peticiones cada duration unit, por cliente.
// Lo lee RateLimitingInterceptor antes de llamar al método. Por ejemplo @RateLimited(capacity = 5) son 5 por minuto

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    long capacity() default 60;
    long duration() default 1;
    ChronoUnit unit() default ChronoUnit.MINUTES;
}
