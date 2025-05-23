package es.upm.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoAceptacion {
    private LocalDateTime fechaHorafirma;
    private String justificante;
    private UserDto firmante;
}

