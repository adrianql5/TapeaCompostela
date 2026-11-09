package gal.usc.etse.es.tapeacompostela.configuration;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
// Activa @PreAuthorize y @PostAuthorize en los servicios
@EnableMethodSecurity
// Con order = 0 la transacción envuelve la comprobación de seguridad de los métodos,
// así que si @PostAuthorize deniega (403) la transacción aún está abierta y
// sus cambios se deshacen
@EnableTransactionManagement(order = 0)
public class SecurityConfiguration {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // El claim del token que tiene el nombre del usuario: "sub" en nuestros tokens, "preferred_username" en los de
    // Keycloak (lo cambia application-oidc.properties). Sin la propiedad, "sub"
    @Value("${app.jwt.principal-claim:sub}")
    private String principalClaim;

    // Las contraseñas se guardan hasheadas, nunca en texto plano
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Comprueba un username y su contraseña, Spring lo conecta con nuestro UserDetailsService (UserService) y el encoder
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF protege las cookies de sesión, esta API no tiene sesión, así que no se necesita
                .csrf(csrf -> csrf.disable())
                // Stateless: cada request se comprueba solo por su token
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        // La documentación: el documento de OpenAPI y Swagger UI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Cualquiera puede leer los negocios, las etiquetas, las reseñas y las recompensas; el resto necesita un token
                        .requestMatchers(HttpMethod.GET, "/businesses/**", "/tags", "/reviews", "/reviews/**", "/rewards", "/rewards/**").permitAll()
                        .anyRequest().authenticated()
                )
                // El filtro de Spring que lee "Authorization: Bearer <token>" y comprueba el JWT con el JwtDecoder
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    // Usado por el filtro de Spring para comprobar la firma y la expiración del token en cada request
    // Con el perfil "oidc" no se crea: Spring Boot crea el suyo a partir del issuer-uri, con las claves públicas de Keycloak
    @Bean
    @Profile("!oidc")
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    // Usado por JwtService para firmar los tokens del login
    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    // Los roles del JWT se convierten en las autoridades ROLE_USER, ROLE_OWNER... de Spring Security.
    // Se leen de los dos sitios: nuestros tokens los tienen en el claim "roles": ["USER", "OWNER"], y los de
    // Keycloak en "realm_access": {"roles": ["USER", "OWNER"]}. Así hasRole('OWNER') funciona con los dos
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setPrincipalClaimName(principalClaim);
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            }

            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List<?> keycloakRoles) {
                keycloakRoles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase())));
            }
            return authorities;
        });
        return jwtConverter;
    }
}