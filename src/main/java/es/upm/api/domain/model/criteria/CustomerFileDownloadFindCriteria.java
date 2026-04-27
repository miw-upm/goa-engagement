package es.upm.api.domain.model.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFileDownloadFindCriteria {
    private String customer;
    private String documentType;

    public boolean all() {
        return customer == null && documentType == null;
    }
}
