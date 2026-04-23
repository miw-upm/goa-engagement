package es.upm.api.domain.model.snapshots;

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
    UUID id;
}
