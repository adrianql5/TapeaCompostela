package gal.usc.etse.es.tapeacompostela.model.dto;

import jakarta.validation.constraints.NotNull;

// The user inside other resources, like Author inside Book in the course example.
// In requests only the id is read, e.g. {"user": {"id": 4}}
public record UserSummary(
        @NotNull Long id,
        String username,
        String name,
        String avatarUrl
) {
    public static UserSummary from(gal.usc.etse.es.tapeacompostela.model.entity.User user) {
        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getAvatarUrl()
        );
    }
}
