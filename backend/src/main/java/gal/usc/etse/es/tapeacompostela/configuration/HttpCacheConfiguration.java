package gal.usc.etse.es.tapeacompostela.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
public class HttpCacheConfiguration {

    // Añade un ETag (hash del body) a cada respuesta de los GET. Si el cliente
    // lo manda de nuevo en If-None-Match y el body no ha cambiado, la respuesta
    // es 304 Not Modified sin body. Ahorra red, no trabajo: el controlador
    // se ejecuta igual para calcular el body
    // Menos GET /redemptions/events (fase 10): es un stream de eventos que no termina nunca. El filtro guarda la
    // respuesta entera para calcular su hash antes de mandarla, así que los eventos se quedarían ahí y no llegarían
    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter() {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                return request.getRequestURI().equals("/redemptions/events");
            }
        };
    }
}