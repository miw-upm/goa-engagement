package es.upm.api.domain.services;

import es.upm.api.domain.model.*;
import es.upm.api.domain.model.criteria.EngagementLetterFindCriteria;
import es.upm.api.domain.model.external.AccessLinkSnapshot;
import es.upm.api.domain.model.external.UserSnapshot;
import es.upm.api.domain.ports.out.email.EmailWriter;
import es.upm.api.domain.ports.out.legal.EngagementLetterGateway;
import es.upm.api.domain.ports.out.user.AccessLinkGateway;
import es.upm.api.domain.ports.out.user.UserFinder;
import es.upm.miw.exception.BadGatewayException;
import es.upm.miw.exception.InvalidTransitionException;
import es.upm.miw.pdf.PdfBuilder;
import es.upm.miw.pdf.TextDictionary;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Element;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EngagementLetterService {
    private final EngagementLetterGateway engagementLetterGateway;
    private final AccessLinkGateway accessLinkGateway;
    private final UserFinder userFinder;
    private final CustomerFileDownloadService customerFileDownloadService;
    private final EmailWriter emailWriter;
    private final SignedEngagementLetterEmailTemplateService signedEngagementLetterEmailTemplateService;

    public void create(EngagementLetter engagementLetter) {
        engagementLetter.setId(UUID.randomUUID());
        engagementLetter.setOwner(
                this.userFinder.readByMobile(engagementLetter.getOwner().getMobile())
        );
        engagementLetter.setLastUpdatedDate(LocalDate.now());
        if (engagementLetter.getAttachments() != null) {
            engagementLetter.getAttachments().forEach(attachment -> attachment.setId(this.userFinder.readByMobile(attachment.getMobile()).getId()));
        }
        this.engagementLetterGateway.create(engagementLetter);
    }

    public EngagementLetter readById(UUID id) {
        EngagementLetter engagementLetter = this.engagementLetterGateway.readById(id);
        engagementLetter.setOwner(
                this.userFinder.readById(engagementLetter.getOwner().getId())
        );
        Optional.ofNullable(engagementLetter.getAttachments())
                .ifPresent(attachments -> engagementLetter.setAttachments(
                        attachments.stream()
                                .map(userDto -> this.userFinder.readById(userDto.getId()))
                                .toList()
                ));
        return engagementLetter;
    }

    public void update(UUID id, EngagementLetter engagementLetter) {
        engagementLetter.setLastUpdatedDate(LocalDate.now());
        engagementLetter.setId(id);
        this.engagementLetterGateway.update(id, engagementLetter);
    }

    public void delete(UUID id) {
        this.engagementLetterGateway.delete(id);
    }

    public Stream<EngagementLetter> find(EngagementLetterFindCriteria criteria) {
        Stream<EngagementLetter> letters = this.engagementLetterGateway.find(criteria);

        if (StringUtils.hasText(criteria.getClient())) {
            List<UUID> clientIds = this.userFinder.find(criteria.getClient()).stream()
                    .map(UserSnapshot::getId)
                    .toList();
            letters = letters.filter(letter -> letter.isClientInLetter(clientIds));
        }
        return letters
                .map(letter -> {
                    letter.setOwner(this.userFinder.readById(letter.getOwner().getId()));
                    return letter;
                });
    }

    public Stream<UserSnapshot> findPendingSigners(UUID id) {
        EngagementLetter letter = this.readById(id);
        if (Boolean.TRUE.equals(letter.getBudgetOnly())) {
            throw new InvalidTransitionException("Un presupuesto no puede ser firmado");
        }
        if (!letter.areAllUsersComplete()) {
            throw new InvalidTransitionException("Para poder firmar, tanto el propietario como los adjuntos deben estar totalmente completados");
        }
        List<UserSnapshot> pendingSigners = letter.findPendingSigners();
        if (pendingSigners.isEmpty()) {
            throw new InvalidTransitionException("Todos los intervinientes ya han firmado");
        }
        return pendingSigners.stream();
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
                .paragraph(dict.getText("aviso_hoja"));
        if (letter.isSigned()) {
            List<PdfBuilder.LeftSignature> leftSignatures = letter.getAcceptanceEngagements().stream()
                    .map(ae -> new PdfBuilder.LeftSignature(ae.toDonFullName(),
                            String.format("Firmado electrónicamente %s (CET)%nRef.: %s",
                                    ae.getSignatureAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                                    ae.suffix()
                            )
                    )).toList();
            pdf.multiSignatureWithSignatures(leftSignatures, dict.getText("firma_nuria"));
        } else {
            pdf.multiSignature(letter.buildClientsName(), dict.getText("firma_nuria"));
        }
    }

    public byte[] readPdfWithToken(String scope, String urlId, String token) {
        AccessLinkSnapshot accessLink = this.accessLinkGateway.consume(scope, urlId, token);
        UserSnapshot user = this.userFinder.readByUrlIdWithToken(scope, urlId, token);
        CustomerFileDownload customerFileDownload = CustomerFileDownload.builder()
                .customer(user)
                .documentType(scope)
                .documentId(accessLink.getDocumentId())
                .downloadToken(token).build();
        this.customerFileDownloadService.create(customerFileDownload);
        return this.generatePdf(accessLink.getDocumentId());
    }

    public void signWithToken(String scope, String urlId, AcceptanceEngagement acceptance) {
        AccessLinkSnapshot accessLink = this.accessLinkGateway
                .consume(scope, urlId, acceptance.getSignatureToken());
        UserSnapshot user = this.userFinder.readByUrlIdWithToken(scope, urlId, acceptance.getSignatureToken());
        acceptance.setSignatureAt(LocalDateTime.now());
        acceptance.setSignerId(user.getId());
        acceptance.setSignerFullName(user.toFullName());
        acceptance.setSignerIdentity(user.getIdentity());
        acceptance.setMobile(user.getMobile());
        acceptance.setSignerEmail(user.getEmail());
        EngagementLetter letter = this.engagementLetterGateway.readById(accessLink.getDocumentId());
        letter.add(acceptance);
        this.engagementLetterGateway.update(letter.getId(), letter);
        if (letter.isSigned()) {
            this.sendEmails(letter);
        }
    }

    private void sendEmails(EngagementLetter letter) {
        try {
            this.sendEmail(letter.getOwner());
            letter.getAttachments().forEach(this::sendEmail);
        } catch (FeignException.BadRequest e) {
            throw new BadGatewayException("Error de email", e.getCause());
        } catch (Exception e) {
            throw new BadGatewayException("Error del host de email", e.getCause());
        }
    }

    private void sendEmail(UserSnapshot user) {
        this.emailWriter.sendHtml(
                this.signedEngagementLetterEmailTemplateService.buildHtmlEmail(
                        user.getEmail(),
                        user.getFirstName()
                )
        );
    }
}
