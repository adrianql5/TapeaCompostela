package gal.usc.etse.es.tapeacompostela.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
public class HttpCacheConfiguration {

    // Añade un ETag (hash del body) a cada respuesta de los GET. Si el cliente
    // lo manda de nuevo en If-None-Match y el body no ha cambiado, la respuesta
    // es 304 Not Modified sin body. Ahorra red, no trabajo: el controlador
    // se ejecuta igual para calcular el body
    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }
}