package gal.usc.etse.es.tapeacompostela.model.dto;

import java.time.LocalDateTime;

public record UserBadge(
        Badge badge,
        LocalDateTime earnedAt
) {
    public static UserBadge from(gal.usc.etse.es.tapeacompostela.model.entity.UserBadge userBadge) {
        return new UserBadge(Badge.from(userBadge.getBadge()), userBadge.getEarnedAt());
    }
}
