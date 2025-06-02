package es.upm.api.infrastructure.resources.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalTasksDto {
    List<String> legalTasks;
}
