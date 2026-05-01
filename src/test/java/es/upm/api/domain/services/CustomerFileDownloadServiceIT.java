package es.upm.api.domain.services;

import es.upm.api.adapter.out.user.feign.GoaUserClient;
import es.upm.api.domain.model.CustomerFileDownload;
import es.upm.api.domain.model.criteria.CustomerFileDownloadFindCriteria;
import es.upm.api.domain.model.external.UserSnapshot;
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

import static es.upm.api.configurations.DatabaseSeederDev.C_0;
import static es.upm.api.configurations.DatabaseSeederDev.C_1;
import static es.upm.api.configurations.DatabaseSeederDev.C_2;
import static es.upm.api.configurations.DatabaseSeederDev.UUIDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
@ActiveProfiles("test")
class CustomerFileDownloadServiceIT {

    @Autowired
    private CustomerFileDownloadService customerFileDownloadService;

    @MockitoBean
    private GoaUserClient userFinderClient;

    @BeforeEach
    void setUpMocks() {
        BDDMockito.given(this.userFinderClient.readUserById(any(UUID.class)))
                .willAnswer(invocation -> mockedUser(invocation.getArgument(0)));
        BDDMockito.given(this.userFinderClient.findUser(any(String.class)))
                .willReturn(List.of());
    }

    @Test
    void shouldReadFromSeederAndHydrateCustomer() {
        CustomerFileDownload result = this.customerFileDownloadService.read(UUIDS[0]);

        assertThat(result)
                .isNotNull()
                .satisfies(download -> {
                    assertThat(download.getId()).isEqualTo(UUIDS[0]);
                    assertThat(download.getDocumentType()).isEqualTo("engagement-letter");
                    assertThat(download.getDocumentId()).isEqualTo(UUIDS[0]);
                    assertThat(download.getCustomer()).isNotNull();
                    assertThat(download.getCustomer().getId()).isEqualTo(C_0);
                    assertThat(download.getCustomer().getFirstName()).isEqualTo("c1");
                });
    }

    @Test
    void shouldThrowNotFoundWhenReadNotExists() {
        assertThatThrownBy(() -> this.customerFileDownloadService.read(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFindAllWhenCriteriaIsEmpty() {
        List<CustomerFileDownload> results = this.customerFileDownloadService
                .find(new CustomerFileDownloadFindCriteria(null, null))
                .toList();

        assertThat(results)
                .hasSize(3)
                .extracting(CustomerFileDownload::getId)
                .containsExactly(UUIDS[2], UUIDS[1], UUIDS[0]);
    }

    @Test
    void shouldFindByDocumentTypeContainsIgnoreCase() {
        List<CustomerFileDownload> results = this.customerFileDownloadService
                .find(new CustomerFileDownloadFindCriteria(null, "LETTER"))
                .toList();

        assertThat(results)
                .hasSize(2)
                .allSatisfy(download -> assertThat(download.getDocumentType().toLowerCase()).contains("letter"));
    }

    @Test
    void shouldFindByCustomerAndDocumentTypeContainsIgnoreCase() {
        BDDMockito.given(this.userFinderClient.findUser(eq("c1")))
                .willReturn(List.of(UserSnapshot.builder().id(C_0).build()));

        List<CustomerFileDownload> results = this.customerFileDownloadService
                .find(new CustomerFileDownloadFindCriteria("c1", "BUDGET"))
                .toList();

        assertThat(results)
                .hasSize(1)
                .first()
                .satisfies(download -> {
                    assertThat(download.getId()).isEqualTo(UUIDS[2]);
                    assertThat(download.getCustomer().getId()).isEqualTo(C_0);
                    assertThat(download.getDocumentType().toLowerCase()).contains("budget");
                });
    }

    @Test
    void shouldReturnEmptyWhenCustomerFilterDoesNotMatch() {
        BDDMockito.given(this.userFinderClient.findUser(eq("none")))
                .willReturn(List.of(UserSnapshot.builder().id(C_2).build()));

        List<CustomerFileDownload> results = this.customerFileDownloadService
                .find(new CustomerFileDownloadFindCriteria("none", null))
                .toList();

        assertThat(results).isEmpty();
    }

    private UserSnapshot mockedUser(UUID id) {
        if (C_0.equals(id)) {
            return UserSnapshot.builder().id(C_0).mobile("666666000").firstName("c1").familyName("family-c1")
                    .identity("66666603E").email("c1@gmail.com").build();
        }
        if (C_1.equals(id)) {
            return UserSnapshot.builder().id(C_1).mobile("666666001").firstName("c2").familyName("family-c2")
                    .identity("66666604T").email("c2@gmail.com").build();
        }
        return UserSnapshot.builder().id(C_2).mobile("666666002").firstName("c3").familyName("family-c3")
                .identity("66666605R").email("c3@gmail.com").build();
    }
}
