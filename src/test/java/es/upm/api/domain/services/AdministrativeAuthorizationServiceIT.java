package es.upm.api.domain.services;

import es.upm.api.domain.model.AdministrativeAuthorization;
import es.upm.api.domain.model.criteria.AdministrativeAuthorizationFindCriteria;
import es.upm.api.domain.model.external.AccessLinkSnapshot;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.api.domain.ports.out.user.AccessLinkGateway;
import es.upm.api.domain.ports.out.user.UserFinder;
import es.upm.miw.exception.ForbiddenException;
import es.upm.miw.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static es.upm.api.configurations.DatabaseSeederDev.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
@ActiveProfiles("test")
class AdministrativeAuthorizationServiceIT {

    @Autowired
    private AdministrativeAuthorizationService administrativeAuthorizationService;

    @MockitoBean
    private UserFinder userFinder;

    @MockitoBean
    private AccessLinkGateway accessLinkGateway;

    @BeforeEach
    void setUpMocks() {
        BDDMockito.given(this.userFinder.readById(any(UUID.class)))
                .willAnswer(invocation -> mockedUserById(invocation.getArgument(0)));
        BDDMockito.given(this.userFinder.find(any(String.class)))
                .willReturn(List.of());
        BDDMockito.given(this.userFinder.readByUrlIdWithToken(any(String.class), any(String.class), any(String.class)))
                .willReturn(mockedUserById(C_0));
    }

    @Test
    void shouldReadAdministrativeAuthorizationFromSeeder() {
        AdministrativeAuthorization result = this.administrativeAuthorizationService.read(ID_14);

        assertThat(result)
                .isNotNull()
                .satisfies(authorization -> {
                    assertThat(authorization.getId()).isEqualTo(ID_14);
                    assertThat(authorization.getAuthorizingCustomers()).hasSize(1);
                    assertThat(authorization.getAuthorizingCustomers().getFirst().getId()).isEqualTo(C_0);
                    assertThat(authorization.getAuthorizedRepresentatives()).hasSize(1);
                    assertThat(authorization.getPurpose()).containsIgnoringCase("bancarias");
                    assertThat(authorization.isSigned()).isFalse();
                });
    }

    @Test
    void shouldThrowNotFoundWhenReadNotExists() {
        assertThatThrownBy(() -> this.administrativeAuthorizationService.read(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFindByPurposeIgnoringCase() {
        List<AdministrativeAuthorization> results = this.administrativeAuthorizationService
                .find(new AdministrativeAuthorizationFindCriteria(null, "BANCARIAS", null))
                .toList();

        assertThat(results)
                .isNotEmpty()
                .allSatisfy(authorization ->
                        assertThat(authorization.getPurpose().toLowerCase()).contains("bancarias"));
    }

    @Test
    void shouldFindByClientUsingSeederUsers() {
        BDDMockito.given(this.userFinder.find(eq("cliente0")))
                .willReturn(List.of(UserSnapshot.builder().id(C_0).build()));

        List<AdministrativeAuthorization> results = this.administrativeAuthorizationService
                .find(new AdministrativeAuthorizationFindCriteria("cliente0", null, null))
                .toList();

        assertThat(results)
                .isNotEmpty()
                .allSatisfy(authorization ->
                        assertThat(authorization.getAuthorizingCustomers())
                                .extracting(UserSnapshot::getId)
                                .contains(C_0));
    }

    @Test
    void shouldFindPendingSignersFromSeederAuthorization() {
        List<UserSnapshot> pending = this.administrativeAuthorizationService.findPendingSigners(ID_14).toList();

        assertThat(pending)
                .hasSize(1)
                .first()
                .satisfies(user -> assertThat(user.getId()).isEqualTo(C_0));
    }

    @Test
    void shouldReadOnlyPurposeWhenTokenIsValid() {
        String scope = "administrativeAuthorizations";
        String urlId = "url-id";
        String token = "token";
        BDDMockito.given(this.accessLinkGateway.consume(scope, urlId, token))
                .willReturn(AccessLinkSnapshot.builder().documentId(ID_14).build());
        BDDMockito.given(this.userFinder.readByUrlIdWithToken(scope, urlId, token))
                .willReturn(mockedUserById(C_0));

        AdministrativeAuthorization result = this.administrativeAuthorizationService
                .readAuthorizationPurposeWithToken(scope, urlId, token);

        assertThat(result.getPurpose()).containsIgnoringCase("bancarias");
        assertThat(result.getId()).isNull();
    }

    @Test
    void shouldThrowInvalidTransitionWhenTokenUserIsNotAuthorizingCustomer() {
        String scope = "administrativeAuthorizations";
        String urlId = "url-id";
        String token = "token";
        BDDMockito.given(this.accessLinkGateway.consume(scope, urlId, token))
                .willReturn(AccessLinkSnapshot.builder().documentId(ID_14).build());
        BDDMockito.given(this.userFinder.readByUrlIdWithToken(scope, urlId, token))
                .willReturn(mockedUserById(C_2));

        assertThatThrownBy(() -> this.administrativeAuthorizationService.readAuthorizationPurposeWithToken(scope, urlId, token))
                .isInstanceOf(ForbiddenException.class);
    }

    private UserSnapshot mockedUserById(UUID id) {
        if (C_0.equals(id)) {
            return UserSnapshot.builder()
                    .id(C_0).mobile("600000100").firstName("cliente0").familyName("Garcia Lopez")
                    .identity("66666603E").email("c0@gmail.com").build();
        }
        if (C_1.equals(id)) {
            return UserSnapshot.builder()
                    .id(C_1).mobile("600000101").firstName("Manager1").familyName("Martinez Ruiz")
                    .identity("66666604T").email("m1@gmail.com").build();
        }
        return UserSnapshot.builder()
                .id(C_2).mobile("600000102").firstName("cliente2").familyName("Perez Diaz")
                .identity("66666605R").email("c2@gmail.com").build();
    }
}

