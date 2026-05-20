package es.upm.api.adapter.out.legal.mongo.administrativeauthorization;

import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.model.external.UserSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class AdministrativeAuthorizationEntity {
    @Id
    private UUID id;
    private LocalDate lastUpdatedDate;
    private List<UUID> authorizingCustomerIds;
    private List<UUID> authorizedRepresentativeIds;
    private String purpose;
    private List<AdministrativeAuthorizationSignatureEntity> signatureEntities;

    public AdministrativeAuthorizationEntity(AdministrativeAuthorization administrativeAuthorization) {
        BeanUtils.copyProperties(administrativeAuthorization, this);
        Optional.ofNullable(administrativeAuthorization.getAuthorizingCustomers())
                .ifPresent(customers -> this.authorizingCustomerIds = customers.stream()
                        .map(UserSnapshot::getId)
                        .toList());
        Optional.ofNullable(administrativeAuthorization.getAuthorizedRepresentatives())
                .ifPresent(representatives -> this.authorizedRepresentativeIds = representatives.stream()
                        .map(UserSnapshot::getId)
                        .toList());
        Optional.ofNullable(administrativeAuthorization.getSignatures())
                .ifPresent(signatures -> this.signatureEntities = signatures.stream()
                        .map(AdministrativeAuthorizationSignatureEntity::new)
                        .toList());
    }

    public AdministrativeAuthorization toDomain() {
        AdministrativeAuthorization administrativeAuthorization = new AdministrativeAuthorization();
        BeanUtils.copyProperties(this, administrativeAuthorization);
        Optional.ofNullable(this.authorizingCustomerIds)
                .ifPresent(ids -> administrativeAuthorization.setAuthorizingCustomers(ids.stream()
                        .map(userId -> UserSnapshot.builder().id(userId).build())
                        .toList()));
        Optional.ofNullable(this.authorizedRepresentativeIds)
                .ifPresent(ids -> administrativeAuthorization.setAuthorizedRepresentatives(ids.stream()
                        .map(userId -> UserSnapshot.builder().id(userId).build())
                        .toList()));
        Optional.ofNullable(this.signatureEntities)
                .ifPresent(signatures -> administrativeAuthorization.setSignatures(signatures.stream()
                        .map(AdministrativeAuthorizationSignatureEntity::toDomain)
                        .toList()));
        return administrativeAuthorization;
    }
}
