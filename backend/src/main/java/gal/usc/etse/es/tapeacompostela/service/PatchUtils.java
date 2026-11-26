package gal.usc.etse.es.tapeacompostela.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;


// Aplica un JSON Patch (RFC 6902) a cualquier objeto, con la librería java-json-tools
// La librería funciona con Jackson 2, así que necesita un ObjectMapper propio: Spring
// Boot 4 usa Jackson 3 y no crea ninguno de Jackson 2. findAndRegisterModules añade jsr310, el módulo para LocalDateTime

@Service
public class PatchUtils {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    // permitidos: los campos que el patch puede tocar. Una operación sobre otro campo se rechaza antes de aplicar nada
    @SuppressWarnings("unchecked")
    public <T> T applyPatch(T data, List<Map<String, Object>> updates, Set<String> allowed) throws JsonPatchException {
        for (Map<String, Object> update : updates) {
            String field = String.valueOf(update.get("path")).split("/")[1];
            if (!allowed.contains(field)) {
                throw new JsonPatchException("The field " + field + " cannot be changed");
            }
        }
        
        // La lista de mapas pasa a ser un objeto JsonPatch
        JsonPatch operations = mapper.convertValue(updates, JsonPatch.class);
        // El objeto pasa a ser JsonNode
        JsonNode json = mapper.convertValue(data, JsonNode.class);
        // Se le aplican las operaciones al JsonNode
        JsonNode updatedJson = operations.apply(json);

        // El JsonNode modificado se convierte de nuevo a objeto. Un valor del tipo incorrecto
        // no se puede convertir, es error del cliente (400) no del servidor (500)
        try {
            return (T) mapper.convertValue(updatedJson, data.getClass());
        } catch (IllegalArgumentException e) {
            throw new JsonPatchException("A value of the patch has the wrong type");
        }
    }
}   
