package gal.usc.etse.es.tapeacompostela.model.dto;

import java.time.LocalDateTime;
import java.util.Set;

// La contraseña nunca sale del servidor, por lo que no se pone aquí
public record UserDTO(
        Long id,
        String username,
        String name,
        String email,
        Integer points,
        Set<String> roles,
        LocalDateTime createdAt
) {}