package es.upm.api.domain.ports.out.legal;

import es.upm.api.domain.model.CustomerFileDownload;
import es.upm.api.domain.model.criteria.CustomerFileDownloadFindCriteria;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface CustomerFileDownloadGateway {
    CustomerFileDownload readById(UUID id);

    Stream<CustomerFileDownload> find(CustomerFileDownloadFindCriteria criteria, List<UUID> customerIds);

    Stream<CustomerFileDownload> find(CustomerFileDownloadFindCriteria criteria);
}
