package gal.usc.etse.es.tapeacompostela.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import gal.usc.etse.es.tapeacompostela.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

// Comprueba el límite de @RateLimited antes de que se ejecute el método del controlador. Algoritmo token bucket
// (Bucket4j): cada cliente tiene un cubo con capactity fichas por endpoint, cada petición gasta una y el cubo se
// rellena poco a poco. Sin fichas, 429 Too Many Requests con la cabecera Retry-After
@Component
public class RateLimitingInterceptor implements HandlerInterceptor{
    // Un cubo por cliente y endpoint. Caffeine borra los que llevan una hora sin usarse: así no se llena la memoria
    // con los cubos de miles de IPs que solo hicieron una peticion
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // La anotación en el método o, si no tiene, en la clase del controlador
        RateLimited limit = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (limit == null) {
            limit = handlerMethod.getBeanType().getAnnotation(RateLimited.class);
        }
        if (limit == null) {
            return true;   // endpoint sin límite
        }

        RateLimited config = limit;
        String key = clientIp(request) + ":" + handlerMethod.getMethod().toGenericString();
        Bucket bucket = buckets.get(key, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(config.capacity())
                        .refillGreedy(config.capacity(), Duration.of(config.duration(), config.unit()))
                        .build())
                .build());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }
        long waitSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
        throw new RateLimitExceededException(waitSeconds);
    }


    // La IP del cliente. Detrás de un proxy o un túnel 8como el de la demo con los móviles) todas las peticiones
    //llegan desde el proxy, y la IP de verdad viene en la cabecera X-Forwarded-For
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
