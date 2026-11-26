package gal.usc.etse.es.tapeacompostela.event;

// Un canje acaba de marcarse como USED. Lo publica RewardService cuando el dueño valida el QR, y lo escucha
// RedemptionEventsController para avisar al cliente en tiempo real
public record RedemptionUsedEvent(Long redemptionId, String username) {
}