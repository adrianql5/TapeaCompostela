package gal.usc.etse.es.tapeacompostela.controller;

import com.fasterxml.jackson.annotation.JsonView;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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

    // Todos los campos de los negocios
    @GetMapping("/{id}")
    @JsonView(BusinessDTO.Views.Detail.class)
    public BusinessDTO getBusiness(@PathVariable Long id) {
        return businessService.getBusiness(id);
    }

    // 201 created con la cabecera de localización
    @PostMapping
    public ResponseEntity<BusinessDTO> createBusiness(@RequestBody @Valid BusinessDTO business) {
        BusinessDTO created = businessService.createBusiness(business);
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBusiness(@PathVariable Long id) {
        businessService.deleteBusiness(id);
    }
}