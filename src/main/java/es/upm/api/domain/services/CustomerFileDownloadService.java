package es.upm.api.domain.services;

import es.upm.api.adapter.out.user.feign.GoaUserClient;
import es.upm.api.domain.model.CustomerFileDownload;
import es.upm.api.domain.model.criteria.CustomerFileDownloadFindCriteria;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.api.domain.ports.out.legal.CustomerFileDownloadGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomerFileDownloadService {
    private final CustomerFileDownloadGateway customerFileDownloadGateway;
    private final GoaUserClient userFinderClient;

    public CustomerFileDownload readById(UUID id) {
        CustomerFileDownload customerFileDownload = this.customerFileDownloadGateway.readById(id);
        return this.enrichCustomer(customerFileDownload);
    }

    public Stream<CustomerFileDownload> find(CustomerFileDownloadFindCriteria criteria) {
        if (StringUtils.hasText(criteria.getCustomer())) {
            List<UUID> customerIds = this.userFinderClient.findUser(criteria.getCustomer()).stream()
                    .map(UserSnapshot::getId)
                    .toList();
            return this.customerFileDownloadGateway.find(criteria, customerIds).map(this::enrichCustomer);
        } else {
            return this.customerFileDownloadGateway.find(criteria).map(this::enrichCustomer);
        }
    }

    private CustomerFileDownload enrichCustomer(CustomerFileDownload customerFileDownload) {
        Optional.ofNullable(customerFileDownload.getCustomer())
                .map(UserSnapshot::getId)
                .ifPresent(id -> customerFileDownload.setCustomer(this.userFinderClient.readUserById(id)));
        return customerFileDownload;
    }
}
