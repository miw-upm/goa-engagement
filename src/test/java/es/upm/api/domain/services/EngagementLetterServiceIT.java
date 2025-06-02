package es.upm.api.domain.services;

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
class EngagementLetterServiceIT {

    @Autowired
    private EngagementLetterService engagementLetterService;

    @MockitoBean
    private UserWebClient userWebClient;

    @Test
    void testReadSuccess() {
        BDDMockito.given(this.userWebClient.readUserById(any(UUID.class)))
                .willAnswer(invocation ->
                        UserDto.builder().id(invocation.getArgument(0)).mobile("666000666").firstName("mock").build());

        assertThat(engagementLetterService.readById(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0001")))
                .isNotNull()
                .satisfies(retrieveEncargo -> {
                    assertThat(retrieveEncargo.getOwner().getFirstName()).isEqualTo("mock");
                    assertThat(retrieveEncargo.getOwner().getMobile()).isEqualTo("666000666");
                    assertThat(retrieveEncargo.getDiscount()).isEqualTo(20);
                });
    }
}
