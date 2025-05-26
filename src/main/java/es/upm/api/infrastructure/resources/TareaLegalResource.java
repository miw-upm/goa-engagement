package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.TareaLegal;
import es.upm.api.domain.services.TareaLegalService;
import es.upm.api.infrastructure.resources.view.TareasLegalesDto;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Stream;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(TareaLegalResource.TAREAS_LEGALES)
public class TareaLegalResource {
    public static final String TAREAS_LEGALES = "/tareas-legales";
    public static final String ID_ID = "/{id}";
    public static final String TITULOS = "/titulos";

    private final TareaLegalService tareaLegalService;

    @Autowired
    public TareaLegalResource(TareaLegalService tareaLegalService) {
        this.tareaLegalService = tareaLegalService;
    }
    @GetMapping
    public Stream<TareaLegal> findAll() {
        return this.tareaLegalService.findAll();
    }

    @GetMapping(TITULOS)
    public TareasLegalesDto findTitles() {
        return new TareasLegalesDto(this.tareaLegalService.findAll()
                .map(TareaLegal::getTitulo)
                .toList());
    }

    @PostMapping
    public void create(@RequestBody TareaLegal tareaLegal) {
        this.tareaLegalService.create(tareaLegal);
    }

    @DeleteMapping(ID_ID)
    public void deleteByID(@PathVariable UUID id) {
        this.tareaLegalService.deleteById(id);
    }

    @PutMapping(ID_ID)
    public void update(@PathVariable UUID id, @RequestBody TareaLegal tareaLegal) {
        this.tareaLegalService.update(tareaLegal);
    }

}
