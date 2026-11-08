package gal.usc.etse.es.tapeacompostela.controller;

import gal.usc.etse.es.tapeacompostela.model.dto.ReviewDTO;
import gal.usc.etse.es.tapeacompostela.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

// Las reseñas son una colección propia: /reviews, y cada una /reviews/{id}. El negocio es un filtro de la
// colección (?business=3) y un campo del cuerpo al crear una, no forma parte del path
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // Las reseñas de un negocio, la más nueva primero
    @GetMapping
    public Page<ReviewDTO> getReviews(
            @RequestParam Long business,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {
        return reviewService.getReviews(business, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    // El negocio va en el cuerpo: {"businessId": 3, "rating": 4, "comment": "..."}
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@RequestBody @Valid ReviewDTO review, Authentication authentication) {
        ReviewDTO created = reviewService.createReview(review, authentication.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ReviewDTO getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }
}