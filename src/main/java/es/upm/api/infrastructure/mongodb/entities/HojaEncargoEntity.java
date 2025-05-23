package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.HojaEncargo;
import es.upm.api.domain.model.UserDto;
import lombok.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class HojaEncargoEntity {
    @Id
    private UUID id;
    private Integer descuento;
    private LocalDate fechaCreacion;
    private Boolean abierta;
    private UUID propietarioId;
    @Singular
    private List<UUID> adjuntoIds;
    @Singular
    @DBRef
    private List<DocumentoAceptacionEntity> documentoAceptacionEntities;
    @Singular
    @DBRef
    private List<FormaPagoEntity> formaPagoEntities;
    private List<ProcedimientoLegalEntity> procedimientoLegalEntities;

    public HojaEncargoEntity(HojaEncargo hojaEncargo) {
        BeanUtils.copyProperties(hojaEncargo, this);
        this.propietarioId = hojaEncargo.getPropietario().getId();
    }

    public HojaEncargo toHojaEncargo() {
        HojaEncargo hojaEncargo = new HojaEncargo();
        BeanUtils.copyProperties(this, hojaEncargo);
        hojaEncargo.setPropietario(UserDto.builder().id(this.propietarioId).build());

        if (this.adjuntoIds != null) {
            hojaEncargo.setAdjuntos(this.adjuntoIds.stream()
                    .map(adjuntoId -> UserDto.builder().id(adjuntoId).build())
                    .toList());
        }

        if (this.documentoAceptacionEntities != null) {
            hojaEncargo.setDocumentosAceptacion(this.documentoAceptacionEntities.stream()
                    .map(DocumentoAceptacionEntity::toDocumentoAceptacion)
                    .toList());
        }
        hojaEncargo.setFormasPagos(this.formaPagoEntities.stream()
                .map(FormaPagoEntity::toFormaPago)
                .toList());
        hojaEncargo.setProcedimientosLegales(this.procedimientoLegalEntities.stream()
                .map(ProcedimientoLegalEntity::toProcedimientoLegal)
                .toList());
        return hojaEncargo;
    }
}
