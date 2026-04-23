package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.PublicAccessToken;
import es.upm.api.domain.persistence.PublicAccessTokenPersistence;
import es.upm.api.infrastructure.mongodb.entities.PublicAccessTokenEntity;
import es.upm.api.infrastructure.mongodb.repositories.PublicAccessTokenRepository;
import es.upm.miw.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PublicAccessTokenPersistenceMongodb implements PublicAccessTokenPersistence {
    private final PublicAccessTokenRepository publicAccessTokenRepository;

    @Autowired
    public PublicAccessTokenPersistenceMongodb(PublicAccessTokenRepository publicAccessTokenRepository) {
        this.publicAccessTokenRepository = publicAccessTokenRepository;
    }

    @Override
    public PublicAccessToken create(PublicAccessToken publicAccessToken) {
        return this.publicAccessTokenRepository.save(new PublicAccessTokenEntity(publicAccessToken))
                .toPublicAccessToken();
    }

    @Override
    public PublicAccessToken readByToken(String token) {
        return this.publicAccessTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("The PublicAccessToken doesn't exist: " + token))
                .toPublicAccessToken();
    }

    @Override
    public PublicAccessToken update(PublicAccessToken publicAccessToken) {
        return this.publicAccessTokenRepository.save(new PublicAccessTokenEntity(publicAccessToken))
                .toPublicAccessToken();
    }
}
