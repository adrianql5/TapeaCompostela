package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface TagRepository extends JpaRepository<Tag, Long> {
    // Consultas derivadas. Spring escribe el SQL a partir del nombre del método
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
}