package gal.usc.etse.es.tapeacompostela.model.dto;

import java.time.Instant;

// El QR que muestra la app del cliente, y cuándo tiene que pedir uno nuevo. Lo usan el QR de un canje
// (GET /redemptions/{id}/qr) y el QR personal para sumar puntos (GET /users/me/qr)
public record RedemptionQrDTO(String qr, Instant expiresAt) {}
