package gal.usc.etse.es.tapeacompostela.service;

import gal.usc.etse.es.tapeacompostela.exception.ConflictException;
import gal.usc.etse.es.tapeacompostela.exception.NotFoundException;
import gal.usc.etse.es.tapeacompostela.model.dto.ReviewDTO;
import gal.usc.etse.es.tapeacompostela.model.entity.Business;
import gal.usc.etse.es.tapeacompostela.model.entity.Review;
import gal.usc.etse.es.tapeacompostela.repository.BusinessRepository;
import gal.usc.etse.es.tapeacompostela.repository.ReviewRepository;
import gal.usc.etse.es.tapeacompostela.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviews;
    private final BusinessRepository businesses;
    private final UserRepository users;

    public Page<ReviewDTO> getReviews(Long businessId, Pageable page) {
        if (!businesses.existsById(businessId)) {
            throw new NotFoundException("Business", businessId);
        }
        return reviews.findAllByBusinessId(businessId, page).map(this::toDTO);
    }

    public ReviewDTO getReview(Long id) {
        return reviews.findById(id).map(this::toDTO).orElseThrow(() -> new NotFoundException("Review", id));
    }

    // Dos escrituras que van juntas: la nueva reseña y la nueva media del negocio. @Transactional hace que vayan
    // en una misma transacción: si algo falla (una excepción), las dos se deshacen y la media nunca miente
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ReviewDTO createReview(ReviewDTO review, String username) {
        Long businessId = review.businessId();
        Business business = businesses.findById(businessId).orElseThrow(() -> new NotFoundException("Business", businessId));
        if (reviews.existsByUserUsernameAndBusinessId(username, businessId)) {
            throw new ConflictException("You already reviewed business " + businessId);
        }

        Review saved = reviews.save(Review.builder()
                .user(users.findByUsername(username).orElseThrow(() -> new NotFoundException("User", username)))
                .business(business)
                .rating(review.rating())
                .comment(review.comment())
                .build());
        updateRating(business);
        return toDTO(saved);
    }

    // Solo su autor o un admin. Si @PostAuthorize dice que no, el borrado y la nueva media se deshacen
    @Transactional
    @PostAuthorize("returnObject.user() == authentication.name or hasRole('ADMIN')")
    public ReviewDTO deleteReview(Long id) {
        Review review = reviews.findById(id).orElseThrow(() -> new NotFoundException("Review", id));
        Business business = review.getBusiness();
        // A través de la lista del negocio: con orphanRemoval, sacarla de la lista borra la fila
        business.getReviews().remove(review);
        reviews.flush();
        updateRating(business);
        return toDTO(review);
    }

    // La media y el número de reseñas, redondeada a dos decimales (4.666 -> 4.67). Sin save: el negocio se cargó
    // en esta transacción, así que Hibernate guarda sus cambios al hacer commit (dirty checking)
    private void updateRating(Business business) {
        reviews.flush();
        Double average = reviews.averageRating(business.getId());
        business.setRatingCount((int) reviews.countByBusinessId(business.getId()));
        business.setAverageRating(average == null ? BigDecimal.ZERO : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
    }

    private ReviewDTO toDTO(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getBusiness().getId(),
                review.getUser().getUsername(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}