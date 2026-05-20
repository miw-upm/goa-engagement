package es.upm.api.domain.services;

import es.upm.api.adapter.out.user.feign.GoaUserClient;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.LegalProcedure;
import es.upm.api.domain.model.PaymentMethod;
import es.upm.api.domain.model.criteria.EngagementLetterFindCriteria;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.miw.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static es.upm.api.configurations.DatabaseSeederDev.C_0;
import static es.upm.api.configurations.DatabaseSeederDev.C_1;
import static es.upm.api.configurations.DatabaseSeederDev.C_2;
import static es.upm.api.configurations.DatabaseSeederDev.ID_1;
import static es.upm.api.configurations.DatabaseSeederDev.ID_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
class EngagementLetterServiceIT {

    @Autowired
    private EngagementLetterService engagementLetterService;

    @MockitoBean
    private GoaUserClient userFinderClient;
    private EngagementLetter engagementLetter;

    @BeforeEach
    void setUpEngagementLetter() {
        this.engagementLetter = EngagementLetter.builder()
                .discount(15)
                .owner(UserSnapshot.builder().id(C_0).mobile("666666000").firstName("c1").build())
                .legalProcedures(List.of(LegalProcedure.builder()
                        .title("procedimiento")
                        .budget(BigDecimal.TEN)
                        .legalTasks(List.of("tarea"))
                        .build()))
                .paymentMethods(List.of(PaymentMethod.builder().description("Todo").percentage("100%").build()))
                .build();

        BDDMockito.given(this.userFinderClient.readUserByMobile(any(String.class)))
                .willAnswer(invocation -> mockedUserByMobile(invocation.getArgument(0)));
        BDDMockito.given(this.userFinderClient.readUserById(any(UUID.class)))
                .willAnswer(invocation -> mockedUserById(invocation.getArgument(0)));
        BDDMockito.given(this.userFinderClient.findUser(any(String.class)))
                .willReturn(List.of());
        this.engagementLetterService.create(this.engagementLetter);
    }

    @Test
    void testReadSuccess() {
        assertThat(engagementLetterService.read(ID_1))
                .isNotNull()
                .satisfies(retrieveEngagement -> {
                    assertThat(retrieveEngagement.getOwner().getFirstName()).isEqualTo("c1");
                    assertThat(retrieveEngagement.getOwner().getMobile()).isEqualTo("666666000");
                    assertThat(retrieveEngagement.getDiscount()).isEqualTo(20);
                });
    }

    @Test
    void testCreateSuccess() {
        EngagementLetter engagementLetterDb = this.engagementLetterService.read(engagementLetter.getId());
        assertThat(engagementLetterDb)
                .isNotNull()
                .satisfies(engagement -> {
                    assertThat(engagement.getLastUpdatedDate()).isEqualTo(LocalDate.now());
                    assertThat(engagement.getDiscount()).isEqualTo(15);
                    assertThat(engagement.getPaymentMethods()).hasSize(1);
                    assertThat(engagement.getPaymentMethods().getFirst().getDescription()).isEqualTo("Todo");
                    assertThat(engagement.getAttachments()).isNull();
                    assertThat(engagement.getLegalProcedures()).hasSize(1);
                    assertThat(engagement.getLegalProcedures().getFirst().getTitle()).isEqualTo("procedimiento");
                });
    }

    @Test
    void testUpdateSuccess() {
        UUID originalId = this.engagementLetter.getId();
        EngagementLetter updatedEngagementLetter = EngagementLetter.builder()
                .id(originalId)
                .lastUpdatedDate(LocalDate.of(2000, 1, 1))
                .discount(30)
                .owner(engagementLetter.getOwner())
                .legalProcedures(engagementLetter.getLegalProcedures())
                .paymentMethods(List.of(PaymentMethod.builder().description("Actualizado").percentage("20%").build()))
                .build();
        this.engagementLetterService.update(originalId, updatedEngagementLetter);
        EngagementLetter retrieved = this.engagementLetterService.read(originalId);
        assertThat(retrieved)
                .isNotNull()
                .satisfies(letter -> {
                    assertThat(letter.getLastUpdatedDate()).isEqualTo(LocalDate.now());
                    assertThat(letter.getDiscount()).isEqualTo(30);
                    assertThat(letter.getPaymentMethods()).hasSize(1);
                    assertThat(letter.getPaymentMethods().getFirst().getDescription()).isEqualTo("Actualizado");
                });
    }

    @Test
    void testDeleteSuccess() {
        UUID engagementLetterId = this.engagementLetter.getId();
        this.engagementLetterService.delete(engagementLetterId);
        assertThatThrownBy(() -> this.engagementLetterService.read(engagementLetterId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(engagementLetterId.toString());
    }

    @Test
    void testSearchNullSafeReturnsAllWhenCriteriaEmpty() {
        EngagementLetterFindCriteria criteria = new EngagementLetterFindCriteria();
        List<EngagementLetter> results = engagementLetterService.find(criteria).toList();
        assertThat(results).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    void testFindFiltersByOpenedTrue() {
        EngagementLetterFindCriteria criteria = new EngagementLetterFindCriteria();
        criteria.setOpened(true);
        List<EngagementLetter> results = engagementLetterService.find(criteria).toList();
        assertThat(results)
                .isNotEmpty()
                .allSatisfy(letter -> assertThat(letter.getClosingDate()).isNull());
    }

    @Test
    void testFindFiltersByOpenedFalse() {
        EngagementLetterFindCriteria criteria = new EngagementLetterFindCriteria();
        criteria.setOpened(false);
        List<EngagementLetter> results = engagementLetterService.find(criteria).toList();
        assertThat(results)
                .isNotEmpty()
                .allSatisfy(letter -> assertThat(letter.getClosingDate()).isNotNull());
    }

    @Test
    void testFindFiltersByLegalProcedureTitle() {
        EngagementLetterFindCriteria criteria = new EngagementLetterFindCriteria();
        criteria.setLegalProcedureTitle("herencia");
        List<EngagementLetter> results = engagementLetterService.find(criteria).toList();
        assertThat(results)
                .isNotEmpty()
                .allSatisfy(letter -> assertThat(letter.getLegalProcedures())
                        .anyMatch(proc -> proc.getTitle().toLowerCase().contains("herencia")));
    }

    @Test
    void testFindFiltersByTaskTitle() {
        EngagementLetterFindCriteria criteria = new EngagementLetterFindCriteria();
        criteria.setTaskTitle("asesoramiento");
        List<EngagementLetter> results = engagementLetterService.find(criteria).toList();
        assertThat(results)
                .isNotEmpty()
                .allSatisfy(letter -> assertThat(letter.getLegalProcedures())
                        .anyMatch(proc -> proc.getLegalTasks().stream()
                                .anyMatch(task -> task.toLowerCase().contains("asesoramiento"))));
    }

    @Test
    void testSearchNullSafeIgnoresCase() {
        EngagementLetterFindCriteria criteriaUpper = new EngagementLetterFindCriteria();
        criteriaUpper.setLegalProcedureTitle("HERENCIA");
        EngagementLetterFindCriteria criteriaLower = new EngagementLetterFindCriteria();
        criteriaLower.setLegalProcedureTitle("herencia");
        List<EngagementLetter> upper = engagementLetterService.find(criteriaUpper).toList();
        List<EngagementLetter> lower = engagementLetterService.find(criteriaLower).toList();
        assertThat(upper)
                .isNotEmpty()
                .hasSameSizeAs(lower);
    }

    @Test
    void testSearchNullSafeReturnsEmptyWhenNoMatch() {
        EngagementLetterFindCriteria criteria = new EngagementLetterFindCriteria();
        criteria.setLegalProcedureTitle("xyznoexiste999");

        List<EngagementLetter> results = engagementLetterService.find(criteria).toList();

        assertThat(results).isEmpty();
    }

    @Test
    void testFindCombinesFilters() {
        EngagementLetterFindCriteria criteria = new EngagementLetterFindCriteria();
        criteria.setOpened(true);
        criteria.setLegalProcedureTitle("herencia");

        List<EngagementLetter> results = engagementLetterService.find(criteria).toList();

        assertThat(results)
                .isNotEmpty()
                .allSatisfy(letter -> {
                    assertThat(letter.getClosingDate()).isNull();
                    assertThat(letter.getLegalProcedures())
                            .anyMatch(proc -> proc.getTitle().toLowerCase().contains("herencia"));
                });
    }

    @Test
    void testFindFiltersByOwner() {
        BDDMockito.given(this.userFinderClient.findUser("test"))
                .willReturn(List.of(UserSnapshot.builder().id(C_0).build()));

        EngagementLetterFindCriteria criteria = new EngagementLetterFindCriteria();
        criteria.setClient("test");
        List<EngagementLetter> results = engagementLetterService.find(criteria).toList();
        assertThat(results)
                .isNotEmpty()
                .allSatisfy(letter -> assertThat(letter.getOwner().getId()).isEqualTo(C_0));
    }

    @Test
    void testFindPendingSignersLetterNotFound() {
        assertThatThrownBy(() -> this.engagementLetterService.findPendingSigners(UUID.randomUUID()).toList())
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testFindPendingSignersWithoutAttachments() {
        EngagementLetter letter = this.engagementLetterService.read(ID_3);
        List<UserSnapshot> pending = letter.findPendingSigners();
        assertThat(pending)
                .hasSize(1)
                .first()
                .satisfies(user -> assertThat(user.getId()).isEqualTo(C_0));
    }

    @Test
    void testFindPendingSignersWhenAllHaveSigned() {
        EngagementLetter letter = this.engagementLetterService.read(ID_1);
        List<UserSnapshot> pending = letter.findPendingSigners();
        assertThat(pending).isEmpty();
    }

    @Test
    void testIsSignedReturnsTrueWhenAllSignersHaveSigned() {
        EngagementLetter letter = this.engagementLetterService.read(ID_1);
        assertThat(letter.isSigned()).isTrue();
    }

    @Test
    void testIsSignedReturnsFalseWhenSomeSignersArePending() {
        EngagementLetter letter = this.engagementLetterService.read(ID_3);
        assertThat(letter.isSigned()).isFalse();
    }

    @Test
    void testAreAllUsersCompleteReturnsTrueWithSeedUsers() {
        EngagementLetter letter = this.engagementLetterService.read(ID_1);
        assertThat(letter.areAllUsersComplete()).isTrue();
    }

    private UserSnapshot mockedUserById(UUID id) {
        if (C_0.equals(id)) {
            return UserSnapshot.builder().id(C_0).mobile("666666000").firstName("c1").familyName("family-c1")
                    .identity("66666603E").email("c1@gmail.com").build();
        }
        if (C_1.equals(id)) {
            return UserSnapshot.builder().id(C_1).mobile("666666001").firstName("c2").familyName("family-c2")
                    .identity("66666604T").email("c2@gmail.com").build();
        }
        if (C_2.equals(id)) {
            return UserSnapshot.builder().id(C_2).mobile("666666002").firstName("c3").familyName("family-c3")
                    .identity("66666605R").email("c3@gmail.com").build();
        }
        return UserSnapshot.builder().id(C_0).mobile("666666000").firstName("c1").familyName("family-c1")
                .identity("66666603E").email("c1@gmail.com").build();
    }

    private UserSnapshot mockedUserByMobile(String mobile) {
        return switch (mobile) {
            case "666666001" -> mockedUserById(C_1);
            case "666666002" -> mockedUserById(C_2);
            default -> mockedUserById(C_0);
        };
    }

}
