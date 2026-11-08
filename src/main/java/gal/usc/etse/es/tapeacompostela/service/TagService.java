package gal.usc.etse.es.tapeacompostela.service;

import gal.usc.etse.es.tapeacompostela.exception.ConflictException;
import gal.usc.etse.es.tapeacompostela.exception.NotFoundException;
import gal.usc.etse.es.tapeacompostela.model.dto.TagDTO;
import gal.usc.etse.es.tapeacompostela.model.entity.Tag;
import gal.usc.etse.es.tapeacompostela.repository.BusinessRepository;
import gal.usc.etse.es.tapeacompostela.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    private final TagRepository tags;
    private final BusinessRepository businesses;

    // Los tags se leen en cada búsqueda y casi nunca cambian, así que se guardan en caché.
    // Solo la primera llamada ejecuta el método, las siguientes sacan la lista de memoria
    @Cacheable("tags")
    public List<TagDTO> getTags() {
        return tags.findAll(Sort.by("name")).stream().map(tag -> new TagDTO(tag.getId(), tag.getName())).toList();
    }

    // Cualquier cambio sobre los tags vacía la caché, para que el siguiente getTags los vuelva a leer de la base de datos
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(cacheNames = "tags", allEntries = true)
    public TagDTO createTag(TagDTO tag) {
        if (tags.existsByName(tag.name())) {
            throw new ConflictException("Tag " + tag.name() + " already exists");
        }
        Tag saved = tags.save(Tag.builder().name(tag.name()).build());
        return new TagDTO(saved.getId(), saved.getName());
    }

    // Primero se quita el tag de los negocios que lo tienen: las filas de business_tags apuntan a él
    // y la clave foránea impediría borrarlo
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(cacheNames = "tags", allEntries = true)
    public void deleteTag(Long id) {
        Tag tag = tags.findById(id).orElseThrow(() -> new NotFoundException("Tag", id));
        businesses.findAllByTagsId(id).forEach(business -> business.getTags().remove(tag));
        tags.delete(tag);
    }
}