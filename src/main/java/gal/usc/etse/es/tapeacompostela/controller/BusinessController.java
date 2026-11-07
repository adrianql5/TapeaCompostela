package gal.usc.etse.es.tapeacompostela.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.es.tapeacompostela.model.dto.BusinessDTO;
import gal.usc.etse.es.tapeacompostela.model.entity.Business.PriceRange;
import gal.usc.etse.es.tapeacompostela.service.BusinessService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/businesses")
@RequiredArgsConstructor
public class BusinessController {
    private final BusinessService businessService;

    // GET /businesses?name=taberna&priceRange=MODERATE&page=0&size=5&sort=averageRating&direction=DESC
    // Cada parámetro es opcional. La respuesta es una página de negocios, con los campos de la vista de resumen
    @GetMapping
    @JsonView(BusinessDTO.Views.Summary.class)
    public Page<BusinessDTO> getBusinesses(
         @RequestParam(required = false) String name,
         @RequestParam(required = false) PriceRange priceRange,
         @RequestParam(defaultValue = "0") @Min(0) int page,
         @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
         @RequestParam(defaultValue = "name") String sort,
         @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ){
        return businessService.getBusinesses(name, priceRange, PageRequest.of(page, size, Sort.by(direction, sort)));
    }

    // Un negocio con todos sus campos (vista Detail)
    @GetMapping("/{id}")
    @JsonView(BusinessDTO.Views.Detail.class)
    public BusinessDTO getBusiness(@PathVariable Long id) {
        return businessService.getBusiness(id);
    }

    // 201 Created con la cabecera Location apuntando al nuevo negocio
    @PostMapping
    public ResponseEntity<BusinessDTO> createBusiness(@RequestBody @Valid BusinessDTO business, Authentication authentication) {
        BusinessDTO created = businessService.createBusiness(business, authentication.getName());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public BusinessDTO replaceBusiness(@PathVariable Long id, @RequestBody @Valid BusinessDTO business) {
        return businessService.replaceBusiness(id, business);
    }


    // El cuerpo es una lista de operaciones [{"op": "replace", "path": "/phone", "value": "981000000"}] 
    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    @JsonView(BusinessDTO.Views.Detail.class)
    public BusinessDTO updateBusiness(@PathVariable Long id, @RequestBody List<Map<String, Object>> updates) throws JsonPatchException {
        return businessService.updateBusiness(id, updates);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBusiness(@PathVariable Long id) {
        businessService.deleteBusiness(id);
    }
}