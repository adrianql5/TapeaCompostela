package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Product.Category;
import gal.usc.etse.es.tapeacompostela.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByBusinessId(Long businessId, Pageable page);
    Page<Product> findAllByBusinessIdAndCategory(Long businessId, Category category, Pageable page);
    Optional<Product> findByBusinessIdAndName(Long businessId, String name);
    void deleteAllByBusinessId(Long businessId);
}
