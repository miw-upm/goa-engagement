package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.Ticket;
import es.upm.api.domain.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RequestMapping(TicketResource.TICKETS)
public class TicketResource {
    public static final String TICKETS = "/tickets";

    public static final String URL_TOKEN = "/{urlToken}";

    private final TicketService ticketService;

    @Autowired
    public TicketResource(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PreAuthorize(Security.ALL)
    @GetMapping(URL_TOKEN)
    public Ticket read(@PathVariable String urlToken) {
        return this.ticketService.readByUrlToken(urlToken);
    }

}
