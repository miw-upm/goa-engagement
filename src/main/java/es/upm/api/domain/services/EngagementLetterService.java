package es.upm.api.domain.services;

import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.LegalProcedure;
import es.upm.api.domain.model.PaymentMethod;
import es.upm.api.domain.model.criteria.EngagementLetterCriteria;
import es.upm.api.domain.model.snapshos.UserSnapshot;
import es.upm.api.domain.persistence.EngagementLetterPersistence;
import es.upm.api.domain.webclients.UserWebClient;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Element;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EngagementLetterService {
    private final EngagementLetterPersistence engagementLetterPersistence;
    private final UserWebClient userWebClient;
    private final ApplicationEventPublisher eventPublisher;

    public EngagementLetter readById(UUID id) {
        EngagementLetter engagementLetter = this.engagementLetterPersistence.readById(id);
        engagementLetter.setOwner(
                this.userWebClient.readUserById(engagementLetter.getOwner().getId())
        );
        Optional.ofNullable(engagementLetter.getAttachments())
                .ifPresent(attachments -> engagementLetter.setAttachments(
                        attachments.stream()
                                .map(userDto -> this.userWebClient.readUserById(userDto.getId()))
                                .toList()
                ));
        return engagementLetter;
    }

    public void create(EngagementLetter engagementLetter) {
        engagementLetter.setId(UUID.randomUUID());
        engagementLetter.setOwner(
                this.userWebClient.readUserByMobile(engagementLetter.getOwner().getMobile())
        );
        engagementLetter.setCreationDate(LocalDate.now());
        if (engagementLetter.getAttachments() != null) {
            engagementLetter.getAttachments().forEach(attachment -> attachment.setId(this.userWebClient.readUserByMobile(attachment.getMobile()).getId()));
        }
        this.engagementLetterPersistence.create(engagementLetter);
    }

    public void delete(UUID id) {
        this.engagementLetterPersistence.delete(id);
    }

    public void update(UUID id, EngagementLetter engagementLetter) {
        this.engagementLetterPersistence.update(id, engagementLetter);
    }

    public Stream<EngagementLetter> searchNullSafe(EngagementLetterCriteria criteria) {
        Stream<EngagementLetter> letters = this.engagementLetterPersistence.searchNullSafe(criteria);

        if (StringUtils.hasText(criteria.getClient())) {
            List<UUID> clientIds = this.userWebClient.findNullSafe(criteria.getClient()).stream()
                    .map(UserSnapshot::getId)
                    .toList();
            letters = letters.filter(letter -> isClientInLetter(letter, clientIds));
        }

        return letters;
    }

    private boolean isClientInLetter(EngagementLetter letter, List<UUID> clientIds) {
        if (letter.getOwner() != null && clientIds.contains(letter.getOwner().getId())) {
            return true;
        }
        if (letter.getAttachments() != null) {
            return letter.getAttachments().stream()
                    .anyMatch(attachment -> clientIds.contains(attachment.getId()));
        }
        return false;
    }

    public byte[] generatePdf(UUID engagementLetterId) {
        EngagementLetter letter = this.readById(engagementLetterId);
        TextDictionary dict = new TextDictionary("templates/engagement-letter-texts.txt");
        boolean isBudgetOnly = Boolean.TRUE.equals(letter.getBudgetOnly());
        PdfBuilder pdf = new PdfBuilder()
                .header()
                .space(2)
                .title(dict.getTitle(isBudgetOnly ? "presupuesto" : "hoja"))
                .space()
                .paragraphBold(letter.buildCreationDate(), Element.ALIGN_RIGHT)
                .space();
        if (isBudgetOnly) {
            pdf.paragraphBold(dict.getText("responsable", Map.of("solicitante", letter.getOwner().getFirstName())));
        } else {
            pdf.paragraph(dict.getText("intervinientes", Map.of("clientes", letter.buildClientsFullNameIdentity())));
        }
        buildServicesSection(pdf, dict, letter);
        if (letter.getLegalClause() != null) {
            pdf.space().paragraph(letter.getLegalClause()).space();
        }
        if (isBudgetOnly) {
            buildBudgetFooter(pdf, dict);
        } else {
            buildEngagementLetterFooter(pdf, dict, letter);
        }
        return pdf.footer().build();
    }

    private void buildServicesSection(PdfBuilder pdf, TextDictionary dict, EngagementLetter letter) {
        pdf.section(dict.getText("servicios"));
        for (LegalProcedure procedure : letter.getLegalProcedures()) {
            pdf
                    .paragraphBold(procedure.getTitle())
                    .paragraphBold(procedure.buildFormatBudget())
                    .list(procedure.getLegalTasks())
                    .space();
        }
    }

    private void buildBudgetFooter(PdfBuilder pdf, TextDictionary dict) {
        pdf.space()
                .paragraphBold(dict.getTitle("aviso_presupuesto"))
                .paragraph(dict.getText("aviso_presupuesto"))
                .signatureLine(dict.getText("firma_nuria"));
    }

    private void buildEngagementLetterFooter(PdfBuilder pdf, TextDictionary dict, EngagementLetter letter) {
        pdf.space()
                .paragraphBold(dict.getText("ejecucion_trabajos")).space()
                .section(dict.getTitle("pagos"))
                .list(letter.getPaymentMethods().stream().map(PaymentMethod::toString).toList())
                .section(dict.getTitle("bancos"))
                .list(dict.getList("banco"))
                .section(dict.getTitle("combinacion_vias"))
                .paragraph(dict.getText("combinacion_vias"))
                .section(dict.getTitle("condiciones_generales"))
                .paragraphs(dict.getText("condiciones_generales"))
                .paragraphBold(dict.getText("nota_solidaridad")).space()
                .paragraph(dict.getText("desavenencias")).space()
                .paragraphBold(dict.getTitle("advertencias"))
                .numberedList(dict.getList("advertencia"))
                .section(dict.getTitle("seguro_rc"))
                .paragraph(dict.getText("seguro_rc"))
                .section(dict.getTitle("comunicaciones"))
                .paragraph(dict.getText("comunicaciones"))
                .section(dict.getTitle("proteccion_datos"))
                .paragraph(dict.getText("proteccion_datos"))
                .section(dict.getTitle("jurisdiccion"))
                .paragraph(dict.getText("jurisdiccion")).space(3)
                .paragraphBold(dict.getTitle("aviso_importante"))
                .paragraph(dict.getText("aviso_hoja"))
                .multiSignature(letter.buildClientsName(), dict.getText("firma_nuria"));
    }
}
