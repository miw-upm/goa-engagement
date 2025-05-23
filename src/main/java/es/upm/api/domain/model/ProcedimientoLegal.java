package es.upm.api.domain.model;

import es.upm.api.domain.model.validations.ListNotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcedimientoLegal {
    private UUID id;
    @NotNull
    @NotBlank
    private String titulo;
    private Boolean finalizado;
    private BigDecimal presupuesto;
    private Boolean ivaIncluido;
    @ListNotEmpty
    private List<TareaLegal> tareas;
}
