package gal.usc.etse.es.tapeacompostela.controller;

import gal.usc.etse.es.tapeacompostela.model.dto.*;
import gal.usc.etse.es.tapeacompostela.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Toda la lógica está en AuthService, el controlador solo recibe y responde
@Tag(name = "Auth", description = "Sign up, log in and the tokens of the session")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Sign up. With owner true, also as owner of businesses")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO register(@RequestBody @Valid RegisterRequestDTO data) {
        return authService.register(data);
    }

    @Operation(summary = "Log in: gives the access token (JWT) and the refresh token")
    @PostMapping("/login")
    public TokenResponseDTO login(@RequestBody LoginRequestDTO credentials) {
        return authService.login(credentials);
    }

    @Operation(summary = "Gives a new pair of tokens for a refresh token, which is revoked")
    @PostMapping("/refresh")
    public TokenResponseDTO refresh(@RequestBody RefreshRequestDTO request) {
        return authService.refresh(request.refreshToken());
    }

    @Operation(summary = "Revokes the refresh token")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody RefreshRequestDTO request) {
        authService.logout(request.refreshToken());
    }
}