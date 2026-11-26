package gal.usc.etse.es.tapeacompostela.controller;

import gal.usc.etse.es.tapeacompostela.model.dto.ReviewCursorPageDTO;
import gal.usc.etse.es.tapeacompostela.model.dto.ReviewDTO;
import gal.usc.etse.es.tapeacompostela.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

// Las reseñas son una colección propia: /reviews, y cada una /reviews/{id}. El negocio es un filtro de la
// colección (?business=3) y un campo del cuerpo al crear una, no forma parte del path
@Tag(name = "Reviews", description = "The reviews of the businesses")
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // Las reseñas de un negocio, la más nueva primero, con paginación por cursor: GET /reviews?business=1&limit=2 y después
    // GET /reviews?business=1&limit=2&cursor=<nextCursor de la respuesta anterior>
    @Operation(summary = "The reviews of a business with cursor pagination, the newest first")
    @GetMapping
    public ReviewCursorPageDTO getReviews(
            @RequestParam Long business,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        return reviewService.getReviews(business, cursor, limit);
    }

    // El negocio va en el cuerpo: {"businessId": 3, "rating": 4, "comment": "..."}
    @Operation(summary = "Review a business (once per user). Gives 10 points")
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@RequestBody @Valid ReviewDTO review, Authentication authentication) {
        ReviewDTO created = reviewService.createReview(review, authentication.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "One review")
    @GetMapping("/{id}")
    public ReviewDTO getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }

    @Operation(summary = "Delete a review (its author or an admin)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }
}