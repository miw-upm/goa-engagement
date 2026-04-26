package es.upm.api.domain.model.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessLinkSnapshot {
    private String id;
    private UserSnapshot user;
    private String scope;
    private UUID document;
}
