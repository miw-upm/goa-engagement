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
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpdf.text.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Log4j2
public class EngagementLetterService {
    private final EngagementLetterGateway engagementLetterGateway;
    private final AccessLinkGateway accessLinkGateway;
    private final UserFinder userFinder;
    private final CustomerFileDownloadService customerFileDownloadService;
    private final EmailWriter emailWriter;
    private final SignedEngagementLetterEmailTemplateService signedEngagementLetterEmailTemplateService;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.administration.name}")
    private String name;
    @Value("${app.administration.email}")
    private String email;

    public EngagementLetter create(EngagementLetter engagementLetter) {
        engagementLetter.setId(UUID.randomUUID());
        engagementLetter.setOwner(this.userFinder.readByMobile(engagementLetter.getOwner().getMobile()));
        engagementLetter.setLastUpdatedDate(LocalDate.now());
        if (engagementLetter.getAttachments() != null) {
            engagementLetter.getAttachments().forEach(
                    attachment -> attachment.setId(this.userFinder.readByMobile(attachment.getMobile()).getId())
            );
        }
        return this.engagementLetterGateway.create(engagementLetter);
    }

    public EngagementLetter read(UUID id) {
        EngagementLetter engagementLetter = this.engagementLetterGateway.read(id);
        engagementLetter.setOwner(this.userFinder.readById(engagementLetter.getOwner().getId()));
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
                    Optional.ofNullable(letter.getAttachments())
                            .ifPresent(attachments -> letter.setAttachments(
                                            letter.getAttachments().stream()
                                                    .map(user -> this.userFinder.readById(user.getId()))
                                                    .toList()
                                    )
                            );
                    return letter;
                });
    }

    public Stream<UserSnapshot> findPendingSigners(UUID id) {
        EngagementLetter letter = this.read(id);
        if (Boolean.TRUE.equals(letter.getBudgetOnly())) {
            throw new InvalidTransitionException("Un presupuesto no puede ser firmado");
        }
        if (!letter.areAllUsersComplete()) {
            throw new InvalidTransitionException("Para poder firmar, tanto el propietario como los adjuntos deben estar totalmente completados");
        }
        if (letter.isSigned()) {
            throw new InvalidTransitionException("Todos los intervinientes ya han firmado");
        }
        return letter.findPendingSigners().stream();
    }

    public byte[] generatePdf(UUID engagementLetterId) {
        EngagementLetter letter = this.read(engagementLetterId);
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
        this.buildServicesSection(pdf, dict, letter);
        if (letter.getLegalClause() != null) {
            pdf.space().paragraph(letter.getLegalClause()).space();
        }
        if (isBudgetOnly) {
            this.buildBudgetFooter(pdf, dict);
        } else {
            this.buildEngagementLetterFooter(pdf, dict, letter);
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
                    .map(acceptance -> new PdfBuilder.LeftSignature(acceptance.toDonFullName(),
                            String.format("Firmado electrónicamente %s (CET)%nRef.: %s",
                                    acceptance.getSignatureAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                                    acceptance.suffix()
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

    public boolean hasBeenReadWithToken(String scope, String urlId, String token) {
        AccessLinkSnapshot accessLink = this.accessLinkGateway.consume(scope, urlId, token);
        return this.customerFileDownloadService.existsByDocumentId(accessLink.getDocumentId());
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
        EngagementLetter letter = this.engagementLetterGateway.read(accessLink.getDocumentId());
        letter.add(acceptance);
        this.encode(acceptance);
        this.engagementLetterGateway.update(letter.getId(), letter);

        if (letter.isSigned()) {
            this.sendEmails(letter);
        }
    }

    private void encode(AcceptanceEngagement acceptance) {
        acceptance.setSignerIdentity(this.passwordEncoder.encode(acceptance.getSignerIdentity()));
        acceptance.setSignerEmail(this.passwordEncoder.encode(acceptance.getSignerEmail()));
        acceptance.setSignatureToken(this.passwordEncoder.encode(acceptance.getSignatureToken()));
        acceptance.getDeviceInfo().setIpAddress(this.passwordEncoder.encode(acceptance.getDeviceInfo().getIpAddress()));
    }

    private void sendEmails(EngagementLetter letter) {
        byte[] pdf = this.generatePdf(letter.getId());
        String fileName = "Hoja de Encargo.pdf";
        List<String> failedEmails = this.collectRecipients(letter).stream()
                .map(user -> this.trySendEmail(user, pdf, fileName))
                .flatMap(Optional::stream)
                .toList();

        if (!failedEmails.isEmpty()) {
            String message = "Error enviando emails a: " + String.join(", ", failedEmails) + "."
                    + " Si no recibe una copia firmada por email, contacte con el despacho. Disculpe las molestias.";
            throw new BadGatewayException(message);
        }
    }

    private List<UserSnapshot> collectRecipients(EngagementLetter letter) {
        List<UserSnapshot> recipients = new ArrayList<>();
        recipients.add(UserSnapshot.builder().firstName(this.name).email(this.email).build());
        recipients.add(this.userFinder.readById(letter.getOwner().getId()));
        if (letter.getAttachments() != null) {
            letter.getAttachments().forEach(user ->
                    recipients.add(this.userFinder.readById(user.getId())));
        }
        return recipients;
    }

    private Optional<String> trySendEmail(UserSnapshot user, byte[] pdf, String fileName) {
        try {
            this.sendEmail(user, pdf, fileName);
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(user.getEmail());
        }
    }

    private void sendEmail(UserSnapshot user, byte[] pdf, String fileName) {
        this.emailWriter.sendHtml(
                this.signedEngagementLetterEmailTemplateService.buildHtmlEmail(user.getEmail(), user.getFirstName()),
                pdf,
                fileName
        );
    }

    public void close(UUID id) {
        EngagementLetter engagementLetter = this.engagementLetterGateway.read(id);
        if (engagementLetter.getLegalProcedures().stream()
                .anyMatch(procedure -> procedure.getBudget() == null)) {
            throw new InvalidTransitionException("No se puede cerrar una hoja de encargo con procedimientos sin presupuesto concreto, el % debe resolverse primero");
        }
        engagementLetter.setClosingDate(LocalDate.now());
        this.engagementLetterGateway.update(id, engagementLetter);
    }
}
