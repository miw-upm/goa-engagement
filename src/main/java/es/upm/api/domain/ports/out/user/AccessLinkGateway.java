package es.upm.api.domain.ports.out.user;

import es.upm.api.domain.model.external.AccessLinkSnapshot;

public interface AccessLinkGateway {
    AccessLinkSnapshot use(String id, String mobile, String scope);
}
