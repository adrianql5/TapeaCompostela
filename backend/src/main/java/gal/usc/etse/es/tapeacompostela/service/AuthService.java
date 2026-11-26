package gal.usc.etse.es.tapeacompostela.service;

import gal.usc.etse.es.tapeacompostela.exception.ConflictException;
import gal.usc.etse.es.tapeacompostela.model.dto.LoginRequestDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.RegisterRequestDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.TokenResponseDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.UserDTO;
import gal.usc.etse.es.tapeacompostela.model.entity.RefreshToken;
import gal.usc.etse.es.tapeacompostela.model.entity.Role;
import gal.usc.etse.es.tapeacompostela.model.entity.User;
import gal.usc.etse.es.tapeacompostela.repository.RefreshTokenRepository;
import gal.usc.etse.es.tapeacompostela.repository.RoleRepository;
import gal.usc.etse.es.tapeacompostela.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokens;
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO register(RegisterRequestDTO data) {
        // El username y el email deben estar libres
        if (users.existsByUsername(data.username())) {
            throw new ConflictException("The username " + data.username() + " is already in use");
        }
        if (users.existsByEmail(data.email())) {
            throw new ConflictException("The email " + data.email() + " is already in use");
        }

        // Siempre tiene el rol USER, y OWNER si lo pidió. La contraseña se guarda hasheada
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(roles.getReferenceById("USER"));
        if (Boolean.TRUE.equals(data.owner())) {
            userRoles.add(roles.getReferenceById("OWNER"));
        }
        User saved = users.save(User.builder()
                .username(data.username())
                .name(data.name())
                .email(data.email())
                .password(passwordEncoder.encode(data.password()))
                .roles(userRoles)
                .build());
        return UserService.toDTO(saved);
    }

    @Transactional
    public TokenResponseDTO login(LoginRequestDTO credentials) {
        // El authenticationManager llama a UserService.loadUserByUsername y compara la contraseña con el hash
        // Si falla lanza una BadCredentialsException (401)
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.username(), credentials.password())
        );

        // El usuario es válido: sus roles van en el token sin el prefijo (ROLE_USER -> USER)
        // Spring Security 7 también añade una autoridad FACTOR_PASSWORD (cómo se autenticó el usuario):
        // no es un rol, así que solo se mantienen las que empiezan por ROLE_
        List<String> userRoles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(role -> role.replace("ROLE_", ""))
                .toList();

        // El access token (JWT de corta duración) y el refresh token
        return new TokenResponseDTO(jwtService.generateToken(auth.getName(), userRoles), generateRefreshToken(auth.getName()));
    }

    // Comprueba un refresh token y devuelve un nuevo par de tokens. El usado es revocado
    // por lo que un refresh token robado solo se puede usar una vez
    @Transactional
    public TokenResponseDTO refresh(String refreshToken) {
        // 1. Debe existir y no estar revocado
        RefreshToken token = refreshTokens.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid or revoked refresh token"));

        // 2. No debe haber expirado
        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokens.delete(token);
            throw new BadCredentialsException("The refresh token has expired. Log in again");
        }

        // 3. Rotación: el usado se revoca y se da un nuevo par
        token.setRevoked(true);
        User user = token.getUser();
        List<String> userRoles = user.getRoles().stream().map(Role::getName).toList();
        return new TokenResponseDTO(jwtService.generateToken(user.getUsername(), userRoles), generateRefreshToken(user.getUsername()));
    }

    // Después del logout el refresh token no se puede usar. El JWT sigue funcionando hasta que expira (1 hora como mucho)
    @Transactional
    public void logout(String refreshToken) {
        refreshTokens.findByTokenAndRevokedFalse(refreshToken).ifPresent(token -> token.setRevoked(true));
    }

    // Un refresh token nuevo y aleatorio, guardado en la base de datos, válido durante 7 días
    private String generateRefreshToken(String username) {
        User user = users.findByUsername(username).orElseThrow();
        RefreshToken token = refreshTokens.save(RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build());
        return token.getToken();
    }
}