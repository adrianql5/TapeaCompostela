package gal.usc.etse.es.tapeacompostela.scheduling;

import gal.usc.etse.es.tapeacompostela.model.entity.Redemption.Status;
import gal.usc.etse.es.tapeacompostela.repository.RedemptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

// Un código de canje se puede usar durante app.redemptions.validity (30 días). Esta tarea se ejecuta cada
// app.redemptions.check-interval y marca como EXPIRED los códigos más viejos. Sus puntos no se devuelven
@Slf4j
@Component
@RequiredArgsConstructor
public class RedemptionExpiryJob {
    private final RedemptionRepository redemptions;

    @Value("${app.redemptions.validity}")
    private Duration validity;

    // fixedDelay: espera el intervalo desde el FINAL de la ejecución anterior, así que nunca se solapan.
    // initialDelay: la primera ejecución, 10 segundos después de arrancar la aplicación
    @Scheduled(initialDelayString = "PT10S", fixedDelayString = "${app.redemptions.check-interval}")
    @Transactional
    public void expireRedemptions() {
        var expired = redemptions.findAllByStatusAndRedeemedAtBefore(Status.PENDING, LocalDateTime.now().minus(validity));
        expired.forEach(redemption -> redemption.setStatus(Status.EXPIRED));
        log.info("{} redemption codes expired", expired.size());
    }
}