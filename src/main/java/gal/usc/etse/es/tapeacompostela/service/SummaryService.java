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

// Tres lecturas independinetens que pueden ejecutarse al mismo tiempo, cada una en su poropio hilo (@Async)
// Cada método devuelve de una vez CompletableFuture
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
