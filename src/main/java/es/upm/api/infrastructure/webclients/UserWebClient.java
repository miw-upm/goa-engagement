package es.upm.api.infrastructure.webclients;

import es.upm.api.configurations.FeignConfig;
import es.upm.api.domain.model.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = UserWebClient.TPV_USER, configuration = FeignConfig.class)
public interface UserWebClient {

    String USERS_ID_ID = "/users/{id}";
    String TPV_USER = "goa-user";
    String MOBILE_ID_ID = "/users/mobile/{mobile}";
    String USERS = "/users";

    @GetMapping(USERS_ID_ID)
    UserDto readUserById(@PathVariable UUID id);

    @GetMapping(MOBILE_ID_ID)
    UserDto readUserByMobile(@PathVariable String mobile);

    @GetMapping(USERS)
    List<UserDto> findNullSafe(@RequestParam(required = false) String attribute);

}
