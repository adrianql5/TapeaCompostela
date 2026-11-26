package gal.usc.etse.es.tapeacompostela.controller;

import gal.usc.etse.es.tapeacompostela.event.RedemptionUsedEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Los avisos de los canjes en tiempo real, con Server-Sent Events. La app del cliente abre GET /redemptions/events
// mientras enseña el QR y la conexión se queda abierta: cuando el dueño valida el canje, el servidor le manda el
// evento "redemption-used" al momento, en vez de que la app pregunte cada pocos segundos si ya está usado
@Tag(name = "Rewards", description = "Rewards of the businesses and their redemptions with points")
@RestController
public class RedemptionEventsController {

    // Quién escucha. El usuario se guarda al suscribirse: el evento llega en otro hilo, que no sabe quién hizo la
    // petición (no tiene su Authentication), y cada usuario solo debe recibir los avisos de sus canjes
    private record Subscriber(String username, SseEmitter emitter) {
    }

    // Una lista segura con varios hilos: unos se suscriben mientras otro recorre la lista para mandar un evento
    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();

    @Operation(summary = "Stream of events (text/event-stream) about the redemptions of the logged user")
    @GetMapping(path = "/redemptions/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) throws IOException {
        SseEmitter emitter = new SseEmitter(0L);   // 0: sin tiempo límite, la conexión dura lo que quiera el cliente
        Subscriber subscriber = new Subscriber(authentication.getName(), emitter);
        subscribers.add(subscriber);
        emitter.onCompletion(() -> subscribers.remove(subscriber));
        emitter.onTimeout(() -> subscribers.remove(subscriber));
        emitter.onError(e -> subscribers.remove(subscriber));
        // Un comentario (": connected"), que el cliente ignora. Sin nada que enviar, la respuesta (ni sus cabeceras)
        // no sale hasta el primer evento, y el fetch del navegador se quedaría esperando
        emitter.send(SseEmitter.event().comment("connected"));
        return emitter;
    }

    // AFTER_COMMIT: el aviso sale cuando el canje ya está guardado como USED. Si la app lo pide justo al recibirlo,
    // ya lo ve usado; y si la transacción se deshace, no se avisa de nada
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRedemptionUsed(RedemptionUsedEvent event) {
        for (Subscriber subscriber : subscribers) {
            if (!subscriber.username().equals(event.username())) {
                continue;
            }
            try {
                // Spring convierte el record en JSON: {"redemptionId": 2, "username": "adrianql5"}
                subscriber.emitter().send(SseEmitter.event().name("redemption-used").data(event));
            } catch (IOException e) {
                // El cliente cerró la conexión (cerró la pantalla del QR, se quedó sin red)
                subscribers.remove(subscriber);
            }
        }
    }
}