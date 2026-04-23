package es.upm.api.adapter.out.user.feign;

import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.api.domain.ports.out.user.UserFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserFinderAdapter implements UserFinder {
    private final UserFinderClient userFeignClient;

    @Override
    public UserSnapshot readById(UUID id) {
        return userFeignClient.readUserById(id);
    }

    @Override
    public UserSnapshot readByMobile(String mobile) {
        return userFeignClient.readUserByMobile(mobile);
    }

    @Override
    public List<UserSnapshot> find(String attribute) {
        return userFeignClient.find(attribute);
    }
}
