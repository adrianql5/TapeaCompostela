package gal.usc.etse.es.tapeacompostela.service;

import gal.usc.etse.es.tapeacompostela.exception.NotFoundException;
import gal.usc.etse.es.tapeacompostela.model.dto.BusinessDTO;
import gal.usc.etse.es.tapeacompostela.model.entity.Business;
import gal.usc.etse.es.tapeacompostela.model.entity.Tag;
import gal.usc.etse.es.tapeacompostela.repository.BusinessRepository;
import gal.usc.etse.es.tapeacompostela.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// Read only por defecto
// Los métodos que escriben lo sobreescriben con @Transactional
@Transactional(readOnly = true)
public class BusinessService {
    private final BusinessRepository businesses;
    private final TagRepository tags;

    private static final ExampleMatcher SEARCH = ExampleMatcher.matching().withIgnorePaths("averageRating", "ratingCount").withMatcher("name", match -> match.contains().ignoreCase());

    public Page<BusinessDTO> getBusinesses(String name, Business.PriceRange priceRange, Pageable page){
        
        // Query by example, la sonda solo tiene los campos que le llegaron, el resto los ignora
        // El nombre coincide si contiene el texto  sin distinguir mayúsculas ("taberna" encuentra "A Taberna do Bispo")
        Business probe = new Business();
        probe.setName(name);
        probe.setPriceRange(priceRange);
        // Los campos con valor por defecto (averageRating = 0, ratingCount = 0) no son null, así que formarían parte
        // del filtro: withIgnorePaths los deja fuera. Query by Example no usa las listas
        probe.setAverageRating(null);
        probe.setRatingCount(null);
        return businesses.findAll(Example.of(probe, SEARCH), page).map(this::toDTO);
    }

    public BusinessDTO getBusiness(Long id) {
        return businesses.findById(id).map(this::toDTO).orElseThrow(() -> new NotFoundException("Business", id));
    }

    @Transactional
    public BusinessDTO createBusiness(BusinessDTO business) {
        Business entity = new Business();
        copy(business, entity);
        return toDTO(businesses.save(entity));
    }

    // PUT reemplaza el negocio entero, cada campo tiene el valor que se le pasa en el nuevo negocio
    // si falta algun dato se pone a null
    @Transactional
    public BusinessDTO replaceBusiness(Long id, BusinessDTO business) {
        Business entity = businesses.findById(id).orElseThrow(() -> new NotFoundException("Business", id));
        copy(business, entity);
        // No guardo, la entidad fue cargada en esta transaccion, así que Hibernate la guarda los cambios en el commit
        return toDTO(entity);
    }

    @Transactional
    public void deleteBusiness(Long id) {
        Business entity = businesses.findById(id).orElseThrow(() -> new NotFoundException("Business", id));
        // Sus reseñas y recompensas se borran tambien
        businesses.delete(entity);
    }

    // Solo los campos que el cliente puede cambiar. id, averfaRating, RatingCount, owner y createdAt no.
    private void copy(BusinessDTO business, Business entity) {
        entity.setName(business.name());
        entity.setDescription(business.description());
        entity.setAddress(business.address());
        entity.setLatitude(business.latitude());
        entity.setLongitude(business.longitude());
        entity.setPriceRange(business.priceRange());
        entity.setPhone(business.phone());
        entity.setTags(findTags(business.tags()));
    }

    // El DTO tiene los nombres de las etiquetas, la entidad necesita las filas de Tag. Un tag desconodio es un 404
    private Set<Tag> findTags(Set<String> names) {
        Set<Tag> result = new HashSet<>();
        if (names != null) {
            for (String name : names) {
                result.add(tags.findByName(name).orElseThrow(() -> new NotFoundException("Tag", name)));
            }
        }
        return result;
    }

    private BusinessDTO toDTO(Business business) {
        return new BusinessDTO(
                business.getId(),
                business.getName(),
                business.getDescription(),
                business.getAddress(),
                business.getLatitude(),
                business.getLongitude(),
                business.getPriceRange(),
                business.getPhone(),
                business.getAverageRating(),
                business.getRatingCount(),
                business.getTags().stream().map(Tag::getName).collect(Collectors.toSet()),
                business.getOwner() == null ? null : business.getOwner().getUsername(),
                business.getCreatedAt()
        );
    }
}