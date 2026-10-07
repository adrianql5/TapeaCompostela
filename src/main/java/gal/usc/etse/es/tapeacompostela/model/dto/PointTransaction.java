package gal.usc.etse.es.tapeacompostela.model.dto;

import gal.usc.etse.es.tapeacompostela.model.entity.PointTransaction.Type;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(collectionRelation = "transactions")
public record PointTransaction(
        Long id,
        // The customer. In requests only its id is needed: {"user": {"id": 4}}
        @NotNull @Valid UserSummary user,
        Long businessId,
        @NotNull @Positive Integer points,
        Type type,
        String reason,
        LocalDateTime createdAt,
        UserSummary registeredBy,
        Long redemptionId
) {
    public static PointTransaction from(gal.usc.etse.es.tapeacompostela.model.entity.PointTransaction transaction) {
        return new PointTransaction(
                transaction.getId(),
                UserSummary.from(transaction.getUser()),
                transaction.getBusiness().getId(),
                transaction.getPoints(),
                transaction.getType(),
                transaction.getReason(),
                transaction.getCreatedAt(),
                transaction.getRegisteredBy() == null ? null : UserSummary.from(transaction.getRegisteredBy()),
                transaction.getRedemption() == null ? null : transaction.getRedemption().getId()
        );
    }
}
