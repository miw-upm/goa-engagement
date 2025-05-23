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
    private List<DocumentoAceptacion> documentosAceptacions;
    @Singular
    private List<FormaPago> formaPagos;
    @Singular
    @DBRef
    private List<ProcedimientoLegalEntity> procedimientoLegales;

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

        if (this.documentosAceptacions != null) {
            hojaEncargo.setDocumentosAceptacion(this.documentosAceptacions.stream()
                    .map(DocumentoAceptacion::toDocumentoAceptacion)
                    .toList());
        }
        hojaEncargo.setFormasPagos(this.formaPagos.stream()
                .map(FormaPago::toFormaPago)
                .toList());
        hojaEncargo.setProcedimientosLegales(this.procedimientoLegales.stream()
                .map(ProcedimientoLegalEntity::toProcedimientoLegal)
                .toList());
        return hojaEncargo;
    }
}
