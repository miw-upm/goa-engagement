package es.upm.api.domain.model;

import es.upm.api.domain.model.external.UserSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFileDownload {
    private UUID id;
    private LocalDateTime downloadedAt;
    private UserSnapshot customer;
    private String documentType;
    private UUID documentId;
    private String downloadToken;
}
