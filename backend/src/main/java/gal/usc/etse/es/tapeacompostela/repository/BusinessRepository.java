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
    // Los negocios que tienen el tag con ese id (consulta derivada sobre Business.tags.id)
    List<Business> findAllByTagsId(Long tagId);

    // Los mismos métodos que JpaRepository, pero cargando las relaciones en la misma query en vez de una más
    // por negocio. La búsqueda solo hace el join con el owner: un join con una colección (tags) obligaría
    // a Hibernate a paginar en memoria
    @Override
    @EntityGraph(attributePaths = {"owner"})
    <S extends Business> Page<S> findAll(Example<S> example, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"owner", "tags"})
    Optional<Business> findById(Long id);
}