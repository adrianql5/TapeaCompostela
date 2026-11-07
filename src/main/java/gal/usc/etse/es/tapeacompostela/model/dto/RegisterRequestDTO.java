package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// owner: true para registrarse como propietario de negocios (rol OWNER). Nadie puede registrarse como ADMIN
public record RegisterRequestDTO(
        @NotBlank @Size(min = 3, max = 30) String username,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String password,
        Boolean owner
) {}