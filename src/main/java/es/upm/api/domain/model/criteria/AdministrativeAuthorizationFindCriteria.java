package es.upm.api.domain.model.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeAuthorizationFindCriteria {
    private String client;
    private String authorizationPurpose;
    private Boolean isSigned;
}
