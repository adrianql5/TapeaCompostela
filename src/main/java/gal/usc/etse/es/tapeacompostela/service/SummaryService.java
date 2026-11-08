package gal.usc.etse.es.tapeacompostela.service;

import gal.usc.etse.es.tapeacompostela.model.dto.BusinessDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.ReviewDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.RewardDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// Tres lecturas independientes que se pueden ejecutar a la vez, cada una en su propio hilo (@Async).
// Cada método devuelve enseguida un CompletableFuture, que tendrá el resultado cuando el hilo termine.
// Tienen que estar en otro bean: si el controlador llamase a un método @Async de sí mismo, no pasaría por el
// proxy de Spring y se ejecutaría en el mismo hilo
@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryService {
    private final BusinessService businessService;
    private final ReviewService reviewService;
    private final RewardService rewardService;

    @Async
    public CompletableFuture<BusinessDTO> getBusiness(Long id) {
        log.info("Reading business {} in thread {}", id, Thread.currentThread().getName());
        return CompletableFuture.completedFuture(businessService.getBusiness(id));
    }

    @Async
    public CompletableFuture<List<ReviewDTO>> getLatestReviews(Long id) {
        log.info("Reading the reviews of business {} in thread {}", id, Thread.currentThread().getName());
        return CompletableFuture.completedFuture(reviewService.getReviews(id, null, 3).data());
    }

    @Async
    public CompletableFuture<List<RewardDTO>> getRewards(Long id) {
        log.info("Reading the rewards of business {} in thread {}", id, Thread.currentThread().getName());
        return CompletableFuture.completedFuture(rewardService.getRewards(id));
    }
}
