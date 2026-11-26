package gal.usc.etse.es.tapeacompostela.event;

import gal.usc.etse.es.tapeacompostela.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Da puntos por cada reseña. ReviewService no sabe que esta clase existe: solo publica el evento
@Slf4j
@Component
@RequiredArgsConstructor
public class PointsEventListener {
    public static final int POINTS_PER_REVIEW = 10;

    private final UserRepository users;

    // AFTER_COMMIT: solo si la reseña se guardó (si su transacción se deshace, no hay puntos).
    // @Async: en otro hilo, así que la respuesta del POST /reviews no espera a esto.
    // REQUIRES_NEW: la transacción de la reseña ya terminó, así que hace falta una nueva para guardar los puntos
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onReviewCreated(ReviewCreatedEvent event) {
        log.info("Giving {} points to {} for review {} (thread {})", POINTS_PER_REVIEW, event.username(), event.reviewId(),
                Thread.currentThread().getName());
        users.findByUsername(event.username()).ifPresent(user -> user.setPoints(user.getPoints() + POINTS_PER_REVIEW));
    }
}