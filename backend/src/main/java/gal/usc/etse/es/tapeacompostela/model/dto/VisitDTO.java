package gal.usc.etse.es.tapeacompostela.model.dto;

// Lo que ve el dueño al escanear el QR personal de un cliente (POST /businesses/{id}/visits): a quién le ha dado
// cuántos puntos y en qué local
public record VisitDTO(String user, Long businessId, String business, Integer points) {}
