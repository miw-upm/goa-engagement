package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.TareaLegal;
import es.upm.api.domain.services.TareaLegalService;
import es.upm.api.infrastructure.resources.view.TareasLegalesDto;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(TareaLegalResource.TAREAS_LEGALES)
public class TareaLegalResource {
    public static final String TAREAS_LEGALES = "/tareas-legales";
    public static final String TITULO_ID = "/{titulo}";

    private final TareaLegalService tareaLegalService;

    @Autowired
    public TareaLegalResource(TareaLegalService tareaLegalService) {
        this.tareaLegalService = tareaLegalService;
    }

    @GetMapping
    public TareasLegalesDto findAll() {
        return new TareasLegalesDto(this.tareaLegalService.findAll()
                .map(TareaLegal::getTitulo)
                .toList());
    }

    @PostMapping
    public void create(@RequestBody TareaLegal tareaLegal) {
        this.tareaLegalService.create(tareaLegal);
    }

    @DeleteMapping(TITULO_ID)
    public void deleteByTitulo(@PathVariable String titulo) {
        this.tareaLegalService.deleteByTitulo(titulo);
    }

}
