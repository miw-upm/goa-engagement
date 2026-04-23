package es.upm.api.domain.services;

import es.upm.api.domain.model.criteria.EngagementLetterFindCriteria;
import es.upm.api.domain.model.*;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.api.adapter.out.user.feign.UserFinderClient;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ActiveProfiles("test")
class EngagementLetterServiceIT {

    @Autowired
    private EngagementLetterService engagementLetterService;

    @MockitoBean
    private UserFinderClient userFinderClient;
    private EngagementLetter engagementLetter;

    @BeforeEach
    void setUpEngagementLetter() {
        this.engagementLetter = EngagementLetter.builder()
                .discount(15)
                .owner(UserSnapshot.builder().id(UUID.randomUUID()).mobile("123456789").firstName("John").build())
                .legalProcedures(List.of(LegalProcedure.builder()
                        .title("procedimiento")
                        .budget(BigDecimal.TEN)
                        .legalTasks(List.of("tarea"))
                        .build()))
                .paymentMethods(List.of(PaymentMethod.builder().description("Todo").percentage("100%").build()))
                .build();

        BDDMockito.given(this.userFinderClient.readUserByMobile(any(String.class)))
                .willAnswer(invocation ->
                        UserSnapshot.builder().id(this.engagementLetter.getOwner().getId()).mobile(invocation.getArgument(0)).firstName("mock").build());
        BDDMockito.given(this.userFinderClient.readUserById(any(UUID.class)))
                .willAnswer(invocation ->
                        UserSnapshot.builder().id(invocation.getArgument(0)).mobile("123456789").firstName("mock").build());
        BDDMockito.given(this.userFinderClient.findNullSafe(any(String.class)))
                .willReturn(List.of());
        this.engagementLetterService.create(this.engagementLetter);
    }

    @Test
    void testReadSuccess() {
        assertThat(engagementLetterService.readById(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0001")))
                .isNotNull()
                .satisfies(retrieveEngagement -> {
                    assertThat(retrieveEngagement.getOwner().getFirstName()).isEqualTo("mock");
                    assertThat(retrieveEngagement.getOwner().getMobile()).isEqualTo("123456789");
                    assertThat(retrieveEngagement.getDiscount()).isEqualTo(20);
                });
    }

    @Test
    void testCreateSuccess() {
        EngagementLetter engagementLetterDb = this.engagementLetterService.readById(engagementLetter.getId());
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

        EngagementLetter retrieved = this.engagementLetterService.readById(originalId);
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
        assertThatThrownBy(() -> this.engagementLetterService.readById(engagementLetterId))
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
        UUID ownerId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004");
        BDDMockito.given(this.userFinderClient.findNullSafe("test"))
                .willReturn(List.of(UserSnapshot.builder().id(ownerId).build()));

        EngagementLetterFindCriteria criteria = new EngagementLetterFindCriteria();
        criteria.setClient("test");

        List<EngagementLetter> results = engagementLetterService.find(criteria).toList();

        assertThat(results)
                .isNotEmpty()
                .allSatisfy(letter -> assertThat(letter.getOwner().getId()).isEqualTo(ownerId));
    }
}
