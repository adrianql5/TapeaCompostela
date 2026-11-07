package gal.usc.etse.es.tapeacompostela.repository;

import gal.usc.etse.es.tapeacompostela.model.entity.Business;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    // Los locales que tienen tag, busca por Business.tags y los compada con Tag.id
    List<Business> findAllByTagsId(Long tagId);

    // Los mismos métodos que JpaRepository, pero cargando las relaciones en la misma query en vez de una a mayores
    // por local. La búsqueda solo hace eljoin con el owner.
    @Override
    @EntityGraph(attributePaths = {"owner"})
    <S extends Business> Page<S> findAll(Example<S> example, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"owner", "tags"})
    Optional<Business> findById(Long id);
}