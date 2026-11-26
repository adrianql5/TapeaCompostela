package gal.usc.etse.es.tapeacompostela.model.dto;

import java.time.Instant;

// El QR que muestra la app del cliente, y cuándo tiene que pedir uno nuevo
public record RedemptionQrDTO(String qr, Instant expiresAt) {}
