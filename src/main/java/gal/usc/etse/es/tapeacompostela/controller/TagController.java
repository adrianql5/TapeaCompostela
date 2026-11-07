package gal.usc.etse.es.tapeacompostela.controller;

import gal.usc.etse.es.tapeacompostela.model.dto.TagDTO;
import gal.usc.etse.es.tapeacompostela.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public List<TagDTO> getTags() {
        return tagService.getTags();
    }

    // Siempre 201 cuando funciona
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagDTO createTag(@RequestBody @Valid TagDTO tag) {
        return tagService.createTag(tag);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
    }
}