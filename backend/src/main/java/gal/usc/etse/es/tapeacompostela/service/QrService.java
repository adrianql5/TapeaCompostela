package gal.usc.etse.es.tapeacompostela.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

// El contenido del QR de un canje: "<id>.<window>.<signature>", por ejemplo "2.59200123.kq3V...".
// window es el número del periodo de 30 segundos actual, así que el QR cambia cada 30 segundos. La firma es un HMAC
// del id y la ventana con una clave secreta (la misma idea que el HS256 del JWT): sin la clave nadie
// puede fabricar un QR válido, y una captura de pantalla deja de funcionar en un minuto como mucho
@Service
public class QrService {
    private static final long PERIOD_SECONDS = 30;

    private final SecretKeySpec key;

    public QrService(@Value("${app.qr.secret}") String secret) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String create(Long redemptionId) {
        long window = currentWindow();
        return redemptionId + "." + window + "." + sign(redemptionId, window);
    }

    // Cuándo deja de ser el actual el QR que devuelve create: el inicio de la ventana siguiente
    public Instant nextChange() {
        return Instant.ofEpochSecond((currentWindow() + 1) * PERIOD_SECONDS);
    }

    // Válido solo si es de este canje, de la ventana actual o la anterior (un margen para escanearlo) y con la firma correcta
    public boolean isValid(String qr, Long redemptionId) {
        String[] parts = qr.split("\\.");
        if (parts.length != 3 || !parts[0].equals(String.valueOf(redemptionId))) {
            return false;
        }
        long window;
        try {
            window = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        long now = currentWindow();
        if (window != now && window != now - 1) {
            return false;
        }
        // MessageDigest.isEqual compara en tiempo constante: no da pistas de cuántos caracteres son correctos
        return MessageDigest.isEqual(sign(redemptionId, window).getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8));
    }

    private long currentWindow() {
        return Instant.now().getEpochSecond() / PERIOD_SECONDS;
    }

    private String sign(Long redemptionId, long window) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] signature = mac.doFinal((redemptionId + ":" + window).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
