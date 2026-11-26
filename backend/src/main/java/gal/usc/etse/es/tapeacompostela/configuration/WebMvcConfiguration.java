package gal.usc.etse.es.tapeacompostela.configuration;

import gal.usc.etse.es.tapeacompostela.ratelimit.RateLimitingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


// Registra el interceptor del límite de peticiones en todas las rutas. Solo actúa en los métodos con @RateLimited
@Configuration
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer{
    private final RateLimitingInterceptor rateLimitingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor);
    }
}
