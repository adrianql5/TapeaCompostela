package gal.usc.etse.es.tapeacompostela.controller;

import gal.usc.etse.es.tapeacompostela.model.dto.UserDTO;
import gal.usc.etse.es.tapeacompostela.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "The logged user")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // El usuario logueado: Spring inyecta el Authentication creado a partir del JWT y getName() es su claim sub (el username)
    @Operation(summary = "The logged user, with its points")
    @GetMapping("/me")
    public UserDTO getMe(Authentication authentication) {
        return userService.getUser(authentication.getName());
    }
}