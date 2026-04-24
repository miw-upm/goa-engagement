package es.upm.api.domain.services;

import es.upm.api.adapter.out.user.feign.UserFinderClient;
import es.upm.api.domain.model.AcceptanceEngagement;
import es.upm.api.domain.model.EngagementLetter;
import es.upm.api.domain.model.LegalProcedure;
import es.upm.api.domain.model.PaymentMethod;
import es.upm.api.domain.model.criteria.EngagementLetterFindCriteria;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.api.domain.ports.out.legal.EngagementLetterGateway;
import es.upm.miw.exception.ConflictException;
import es.upm.miw.pdf.PdfBuilder;
import es.upm.miw.pdf.TextDictionary;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Element;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static es.upm.api.configurations.DatabaseSeederDev.UUIDS;

@Service
@RequiredArgsConstructor
public class EngagementLetterService {
    private final EngagementLetterGateway engagementLetterGateway;
    private final UserFinderClient userFinderClient;
    private final ApplicationEventPublisher eventPublisher;

    public EngagementLetter readById(UUID id) {
        EngagementLetter engagementLetter = this.engagementLetterGateway.readById(id);
        engagementLetter.setOwner(
                this.userFinderClient.readUserById(engagementLetter.getOwner().getId())
        );
        Optional.ofNullable(engagementLetter.getAttachments())
                .ifPresent(attachments -> engagementLetter.setAttachments(
                        attachments.stream()
                                .map(userDto -> this.userFinderClient.readUserById(userDto.getId()))
                                .toList()
                ));
        return engagementLetter;
    }

    public void create(EngagementLetter engagementLetter) {
        engagementLetter.setId(UUID.randomUUID());
        engagementLetter.setOwner(
                this.userFinderClient.readUserByMobile(engagementLetter.getOwner().getMobile())
        );
        engagementLetter.setLastUpdatedDate(LocalDate.now());
        if (engagementLetter.getAttachments() != null) {
            engagementLetter.getAttachments().forEach(attachment -> attachment.setId(this.userFinderClient.readUserByMobile(attachment.getMobile()).getId()));
        }
        this.engagementLetterGateway.create(engagementLetter);
    }

    public void delete(UUID id) {
        this.engagementLetterGateway.delete(id);
    }

    public void update(UUID id, EngagementLetter engagementLetter) {
        engagementLetter.setLastUpdatedDate(LocalDate.now());
        engagementLetter.setId(id);
        this.engagementLetterGateway.update(id, engagementLetter);
    }

    public Stream<EngagementLetter> find(EngagementLetterFindCriteria criteria) {
        Stream<EngagementLetter> letters = this.engagementLetterGateway.find(criteria);

        if (StringUtils.hasText(criteria.getClient())) {
            List<UUID> clientIds = this.userFinderClient.find(criteria.getClient()).stream()
                    .map(UserSnapshot::getId)
                    .toList();
            letters = letters.filter(letter -> isClientInLetter(letter, clientIds));
        }
        return letters
                .map(letter -> {
                    letter.setOwner(this.userFinderClient.readUserById(letter.getOwner().getId()));
                    return letter;
                });
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
        TextDictionary dict = new TextDictionary("templates/engagement-letter-texts.yml");
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

    public Stream<UserSnapshot> findPendingSigners(UUID id) {
        EngagementLetter letter = this.readById(id);

        Set<UUID> signedIds = letter.getAcceptanceEngagements() == null
                ? Set.of()
                : letter.getAcceptanceEngagements().stream()
                .filter(AcceptanceEngagement::isSigned)
                .map(AcceptanceEngagement::getSigner)
                .filter(Objects::nonNull)
                .map(UserSnapshot::getId)
                .collect(Collectors.toSet());

        List<UserSnapshot> pendingSigners = Stream.concat(
                        Stream.ofNullable(letter.getOwner()),
                        letter.getAttachments() == null ? Stream.empty() : letter.getAttachments().stream())
                .filter(Objects::nonNull)
                .filter(user -> !signedIds.contains(user.getId()))
                .toList();

        if (pendingSigners.isEmpty()) {
            throw new ConflictException("Todos los firmantes ya han firmado");
        }
        return pendingSigners.stream();
    }

    public byte[] generatePdfWithToken(String mobile, String token) {
        //TODO comprobar mobile y token, y obtener la id
        return this.generatePdf(UUIDS[0]);
    }
}
