package es.upm.api.adapter.out.legal.mongo.customerfiledownload;

import es.upm.api.domain.model.CustomerFileDownload;
import es.upm.api.domain.model.criteria.CustomerFileDownloadFindCriteria;
import es.upm.api.domain.ports.out.legal.CustomerFileDownloadGateway;
import es.upm.miw.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class CustomerFileDownloadAdapter implements CustomerFileDownloadGateway {

    private final CustomerFileDownloadRepository customerFileDownloadRepository;

    @Override
    public CustomerFileDownload readById(UUID id) {
        return this.customerFileDownloadRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The CustomerFileDownload ID doesn't exist: " + id))
                .toDomain();
    }

    @Override
    public Stream<CustomerFileDownload> find(CustomerFileDownloadFindCriteria criteria, List<UUID> customerIds) {
        List<CustomerFileDownloadEntity> entities = StringUtils.hasText(criteria.getDocumentType())
                ? this.customerFileDownloadRepository.findByCustomerIdInAndDocumentType(customerIds, criteria.getDocumentType())
                : this.customerFileDownloadRepository.findByCustomerIdIn(customerIds);
        return entities.stream().map(CustomerFileDownloadEntity::toDomain);
    }

    @Override
    public Stream<CustomerFileDownload> find(CustomerFileDownloadFindCriteria criteria) {
        List<CustomerFileDownloadEntity> entities = StringUtils.hasText(criteria.getDocumentType())
                ? this.customerFileDownloadRepository.findByDocumentType(criteria.getDocumentType())
                : this.customerFileDownloadRepository.findAll(Sort.by(Sort.Direction.DESC, "downloadedAt"));
        return entities.stream().map(CustomerFileDownloadEntity::toDomain);
    }

}
