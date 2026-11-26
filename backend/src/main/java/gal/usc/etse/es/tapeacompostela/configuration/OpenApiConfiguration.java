package gal.usc.etse.es.tapeacompostela.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;


// Los datos generales de OpenAPI. Los endpoints no se escriben aquí, springdoc los lee
// de los controladores cuando la app arranca
@OpenAPIDefinition(
        info = @Info(
                title = "TapeaCompostela API",
                version = "1.0.0",
                description = "Tapas bars of Santiago de Compostela: businesses, tags, reviews and rewards with points",
                contact = @Contact(name = "Adrián Quiroga Linares, Hugo Coto Flórez")
        ),
        servers = @Server(url = "http://localhost:8080", description = "Local development server")
)
@Configuration
public class OpenApiConfiguration {
}