package es.upm.api.infrastructure.resources;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(TareaLegalResource.TAREAS_LEGALES)
public class TareaLegalResource {
    public static final String TAREAS_LEGALES = "/tareas-legales";
    public static final String ID_ID = "/{id}";
}
