package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.DocumentoAceptacion;
import es.upm.api.domain.model.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoAceptacionEntity {
    private LocalDateTime fechaHorafirma;
    private String justificante;
    private UUID userId;

    public DocumentoAceptacionEntity(DocumentoAceptacion aceptacion) {
        BeanUtils.copyProperties(aceptacion, this);
        if (aceptacion.getFirmante() != null) {
            this.userId = aceptacion.getFirmante().getId();
        }
    }

    public DocumentoAceptacion toDocumentoAceptacion() {
        DocumentoAceptacion aceptacion = new DocumentoAceptacion();
        BeanUtils.copyProperties(this, aceptacion);
        aceptacion.setFirmante(UserDto.builder().id(this.userId).build());
        return aceptacion;
    }
}
