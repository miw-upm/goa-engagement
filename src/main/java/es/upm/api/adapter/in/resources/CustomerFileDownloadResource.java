package es.upm.api.adapter.in.resources;

import es.upm.api.domain.model.CustomerFileDownload;
import es.upm.api.domain.model.criteria.CustomerFileDownloadFindCriteria;
import es.upm.api.domain.services.CustomerFileDownloadService;
import es.upm.miw.security.Security;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
@RestController
@RequestMapping(CustomerFileDownloadResource.CUSTOMER_FILE_DOWNLOADS)
@RequiredArgsConstructor
public class CustomerFileDownloadResource {
    public static final String CUSTOMER_FILE_DOWNLOADS = "/customer-file-downloads";
    public static final String ID_ID = "/{id}";

    private final CustomerFileDownloadService customerFileDownloadService;

    @GetMapping(ID_ID)
    public CustomerFileDownload read(@PathVariable UUID id) {
        return this.customerFileDownloadService.readById(id);
    }

    @GetMapping
    public List<CustomerFileDownload> find(@ModelAttribute CustomerFileDownloadFindCriteria criteria) {
        return this.customerFileDownloadService.find(criteria).toList();
    }
}
