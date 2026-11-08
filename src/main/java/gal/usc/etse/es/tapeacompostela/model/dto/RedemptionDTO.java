package gal.usc.etse.es.tapeacompostela.model.dto;

import gal.usc.etse.es.tapeacompostela.model.entity.Redemption.Status;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// Una recompensa canjeada. code es lo que el usuario enseña en el negocio. Para canjear, el cliente solo manda rewardId
public record RedemptionDTO(
        Long id,
        @NotNull Long rewardId,
        String reward,
        Long businessId,
        // El username de quien la canjeó
        String user,
        String code,
        Status status,
        LocalDateTime redeemedAt,
        LocalDateTime usedAt
) {}
