package es.upm.api.infrastructure.mongodb.entities;

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
public class DocumentoAceptacion {
    private LocalDateTime fechaHorafirma;
    private String justificante;
    private UUID userId;

    public DocumentoAceptacion(es.upm.api.domain.model.DocumentoAceptacion aceptacion) {
        BeanUtils.copyProperties(aceptacion, this);
        if (aceptacion.getFirmante() != null) {
            this.userId = aceptacion.getFirmante().getId();
        }
    }

    public es.upm.api.domain.model.DocumentoAceptacion toDocumentoAceptacion() {
        es.upm.api.domain.model.DocumentoAceptacion aceptacion = new es.upm.api.domain.model.DocumentoAceptacion();
        BeanUtils.copyProperties(this, aceptacion);
        aceptacion.setFirmante(UserDto.builder().id(this.userId).build());
        return aceptacion;
    }
}
