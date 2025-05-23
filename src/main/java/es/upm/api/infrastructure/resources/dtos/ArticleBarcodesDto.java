package es.upm.api.infrastructure.resources.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleBarcodesDto {
    private List<String> barcodes;

    public ArticleBarcodesDto(String s) {
    }
}
