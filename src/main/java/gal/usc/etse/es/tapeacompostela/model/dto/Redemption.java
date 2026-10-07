package gal.usc.etse.es.tapeacompostela.model.dto;

import gal.usc.etse.es.tapeacompostela.model.entity.Redemption.Status;
import org.springframework.hateoas.server.core.Relation;

import java.time.Duration;
import java.time.LocalDateTime;

@Relation(collectionRelation = "redemptions")
public record Redemption(
        Long id,
        UserSummary user,
        Long rewardId,
        Long businessId,
        // The code the user shows at the business
        String code,
        Status status,
        LocalDateTime redeemedAt,
        LocalDateTime usedAt,
        // A pending code expires at this moment if it has not been used
        LocalDateTime validUntil
) {
    public static Redemption from(gal.usc.etse.es.tapeacompostela.model.entity.Redemption redemption, Duration validity) {
        return new Redemption(
                redemption.getId(),
                UserSummary.from(redemption.getUser()),
                redemption.getReward().getId(),
                redemption.getReward().getBusiness().getId(),
                redemption.getCode(),
                redemption.getStatus(),
                redemption.getRedeemedAt(),
                redemption.getUsedAt(),
                redemption.getRedeemedAt() == null ? null : redemption.getRedeemedAt().plus(validity)
        );
    }
}
