package es.upm.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamiliaLegal {
    private UUID id;
    private String titulo;
    private List<ProcedimientoLegal> procedimientosLegales;
}
