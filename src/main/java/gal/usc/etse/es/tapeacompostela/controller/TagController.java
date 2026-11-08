package gal.usc.etse.es.tapeacompostela.controller;

import gal.usc.etse.es.tapeacompostela.model.dto.TagDTO;
import gal.usc.etse.es.tapeacompostela.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tags", description = "The tags of the businesses")
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @Operation(summary = "All the tags")
    @GetMapping
    public List<TagDTO> getTags() {
        return tagService.getTags();
    }

    // Siempre 201 cuando funciona
    @Operation(summary = "Create a tag (admins)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagDTO createTag(@RequestBody @Valid TagDTO tag) {
        return tagService.createTag(tag);
    }

    @Operation(summary = "Delete a tag, also from the businesses that have it (admins)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
    }
}