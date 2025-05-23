package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.validations.ListNotEmpty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HojaEncargo {
    private UUID id;
    @Min(0)
    @Max(100)
    private Integer descuento;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCreacion;
    private LocalDate fechaCierre;
    @NotNull
    private UserDto propietario;
    private List<UserDto> adjuntos;
    @ListNotEmpty
    private List<ProcedimientoLegal> procedimientosLegales;
    private List<DocumentoAceptacion> documentosAceptacion;
    private List<FormaPagos> formasPagos;
}
