package gal.usc.etse.es.tapeacompostela.service;

import gal.usc.etse.es.tapeacompostela.exception.ConflictException;
import gal.usc.etse.es.tapeacompostela.exception.NotFoundException;
import gal.usc.etse.es.tapeacompostela.model.dto.TagDTO;
import gal.usc.etse.es.tapeacompostela.model.entity.Tag;
import gal.usc.etse.es.tapeacompostela.repository.BusinessRepository;
import gal.usc.etse.es.tapeacompostela.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    private final TagRepository tags;
    private final BusinessRepository businesses;

    public List<TagDTO> getTags() {
        return tags.findAll(Sort.by("name")).stream().map(tag -> new TagDTO(tag.getId(), tag.getName())).toList();
    }

    @Transactional
    public TagDTO createTag(TagDTO tag) {
        if (tags.existsByName(tag.name())) {
            throw new ConflictException("Tag " + tag.name() + " already exists");
        }
        Tag saved = tags.save(Tag.builder().name(tag.name()).build());
        return new TagDTO(saved.getId(), saved.getName());
    }

    // Los negocios que ya tenian tags los pierden primero, las filas de business_tags apuntan al tag
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tags.findById(id).orElseThrow(() -> new NotFoundException("Tag", id));
        businesses.findAllByTagsId(id).forEach(business -> business.getTags().remove(tag));
        tags.delete(tag);
    }
}