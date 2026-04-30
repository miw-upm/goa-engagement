package es.upm.api.adapter.out.user.feign;

import es.upm.api.domain.model.external.AccessLinkSnapshot;
import es.upm.api.domain.ports.out.user.AccessLinkGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessLinkGatewayAdapter implements AccessLinkGateway {
    private final GoaUserClient goaUserClient;

    @Override
    public AccessLinkSnapshot consume(String scope, String urlId, String token) {
        return goaUserClient.consumeAccessLinkToken(scope, urlId, token);
    }
}
