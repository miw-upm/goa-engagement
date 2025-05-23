package es.upm.api.infrastructure.webclients;

import es.upm.api.configurations.FeignConfig;
import es.upm.api.domain.model.ArticleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = ArticleWebClient.TPV_ARTICLE, configuration = FeignConfig.class)
public interface ArticleWebClient {

    String ARTICLES_ID_ID = "/articles/{id}";
    String TPV_ARTICLE = "tpv-article";

    @GetMapping(ARTICLES_ID_ID)
    ArticleDto readArticleById(@PathVariable UUID id);
}
