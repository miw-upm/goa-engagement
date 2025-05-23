package es.upm.api.domain.services;

import es.upm.api.domain.model.HojaEncargo;
import es.upm.api.domain.model.UserDto;
import es.upm.api.infrastructure.webclients.UserWebClient;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
class HojaEncargoServiceIT {

    @Autowired
    private HojaEncargoService hojaEncargoService;

    @MockitoBean
    private UserWebClient userWebClient;

    @Test
    void testReadSuccess() {
        BDDMockito.given(this.userWebClient.readUserById(any(UUID.class)))
                .willAnswer(invocation ->
                        UserDto.builder().id(invocation.getArgument(0)).mobile("666000666").firstName("mock").build());

        HojaEncargo hojaEncargo = hojaEncargoService.readById(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0001"));
        assertThat(hojaEncargo)
                .isNotNull()
                .satisfies(retrieveEncargo -> {
                    assertThat(retrieveEncargo.getPropietario().getFirstName()).isEqualTo("mock");
                    assertThat(retrieveEncargo.getPropietario().getMobile()).isEqualTo("666000666");
                    assertThat(retrieveEncargo.getDescuento()).isEqualTo(20);
                });
    }
}
