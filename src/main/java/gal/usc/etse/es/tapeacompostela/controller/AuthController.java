package gal.usc.etse.es.tapeacompostela.controller;

import gal.usc.etse.es.tapeacompostela.model.dto.*;
import gal.usc.etse.es.tapeacompostela.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Toda la lógica está en AuthService, el controlador solo recibe y responde
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO register(@RequestBody @Valid RegisterRequestDTO data) {
        return authService.register(data);
    }

    @PostMapping("/login")
    public TokenResponseDTO login(@RequestBody LoginRequestDTO credentials) {
        return authService.login(credentials);
    }

    @PostMapping("/refresh")
    public TokenResponseDTO refresh(@RequestBody RefreshRequestDTO request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody RefreshRequestDTO request) {
        authService.logout(request.refreshToken());
    }
}