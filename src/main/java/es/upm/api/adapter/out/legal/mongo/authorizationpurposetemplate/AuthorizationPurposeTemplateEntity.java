package es.upm.api.adapter.out.legal.mongo.authorizationpurposetemplate;

import es.upm.api.domain.model.AuthorizationPurposeTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class AuthorizationPurposeTemplateEntity {
    @Id
    private UUID id;
    @Indexed(unique = true)
    private String purpose;

    public AuthorizationPurposeTemplateEntity(AuthorizationPurposeTemplate authorizationPurposeTemplate) {
        BeanUtils.copyProperties(authorizationPurposeTemplate, this);
    }

    public AuthorizationPurposeTemplate toDomain() {
        AuthorizationPurposeTemplate authorizationPurposeTemplate = new AuthorizationPurposeTemplate();
        BeanUtils.copyProperties(this, authorizationPurposeTemplate);
        return authorizationPurposeTemplate;
    }
}
