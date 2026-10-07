package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// The current password is not needed when an admin resets the password of another user
public record PasswordChange(
        String currentPassword,
        @NotBlank @Size(min = 6, max = 100) String newPassword
) {}
