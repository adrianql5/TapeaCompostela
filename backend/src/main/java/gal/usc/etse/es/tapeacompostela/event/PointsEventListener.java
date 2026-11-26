package gal.usc.etse.es.tapeacompostela.event;

import gal.usc.etse.es.tapeacompostela.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.resilience.annotation.Retryable;
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
    // REQUIRES_NEW: la transacción de la reseña ya terminó, así que hace falta una nueva para guardar los puntos.
    // @Retryable: si la base de datos falla un momento (un error transitorio: se cae la conexión, un bloqueo),
    // lo vuelve a intentar hasta 3 veces, esperando 0,5 s, 1 s y 2 s. Sin esto los puntos se perderían sin que nadie
    // se entere: este método va en otro hilo y su error no le llega a nadie. Los demás errores no se repiten
    @Async
    @Retryable(includes = TransientDataAccessException.class, maxRetries = 3, delay = 500, multiplier = 2)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onReviewCreated(ReviewCreatedEvent event) {
        log.info("Giving {} points to {} for review {} (thread {})", POINTS_PER_REVIEW, event.username(), event.reviewId(),
                Thread.currentThread().getName());
        users.findByUsername(event.username()).ifPresent(user -> user.setPoints(user.getPoints() + POINTS_PER_REVIEW));
    }
}