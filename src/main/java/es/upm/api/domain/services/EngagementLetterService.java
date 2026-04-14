package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.BadRequestException;
import es.upm.api.domain.model.*;
import es.upm.api.domain.persistence.EngagementLetterPersistence;
import es.upm.api.domain.persistence.PublicAccessTokenPersistence;
import es.upm.api.domain.webclients.UserWebClient;
import org.openpdf.text.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
public class EngagementLetterService {
    public static final int PUBLIC_ACCESS_TOKEN_EXPIRY_DAYS = 5;
    public static final int PUBLIC_ACCESS_TOKEN_MAX_USES = 5;

    private final EngagementLetterPersistence engagementLetterPersistence;
    private final PublicAccessTokenPersistence publicAccessTokenPersistence;
    private final UserWebClient userWebClient;

    @Autowired
    public EngagementLetterService(EngagementLetterPersistence engagementLetterPersistence,
                                   PublicAccessTokenPersistence publicAccessTokenPersistence,
                                   UserWebClient userWebClient) {
        this.engagementLetterPersistence = engagementLetterPersistence;
        this.publicAccessTokenPersistence = publicAccessTokenPersistence;
        this.userWebClient = userWebClient;
    }

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

    public PublicAccessToken createPublicAccessToken(UUID id) {
        EngagementLetter engagementLetter = this.engagementLetterPersistence.readById(id);
        if (engagementLetter.getOwner() == null || engagementLetter.getOwner().getId() == null) {
            throw new BadRequestException("Cannot generate public access token: engagement letter owner is required");
        }
        PublicAccessToken publicAccessToken = PublicAccessToken.builder()
                .id(UUID.randomUUID())
                .token(UUIDBase64.URL.encode())
                .purpose(TokenPurpose.ACCEPT_ENGAGEMENT)
                .expiresAt(LocalDateTime.now().plusDays(PUBLIC_ACCESS_TOKEN_EXPIRY_DAYS))
                .maxUses(PUBLIC_ACCESS_TOKEN_MAX_USES)
                .usedCount(0)
                .isActive(true)
                .engagementLetterId(id)
                .customerId(engagementLetter.getOwner().getId())
                .build();
        return this.publicAccessTokenPersistence.create(publicAccessToken);
    }

    public EngagementLetter readPublicByToken(String token) {
        PublicAccessToken publicAccessToken = this.validatePublicAccessToken(token, "access");
        int currentUsedCount = Optional.ofNullable(publicAccessToken.getUsedCount()).orElse(0);
        publicAccessToken.setUsedCount(currentUsedCount + 1);
        this.publicAccessTokenPersistence.update(publicAccessToken);
        return this.engagementLetterPersistence.readById(publicAccessToken.getEngagementLetterId());
    }

    public EngagementLetter acceptPublicByToken(String token) {
        PublicAccessToken publicAccessToken = this.validatePublicAccessToken(token, "accept");
        EngagementLetter engagementLetter = this.engagementLetterPersistence.readById(publicAccessToken.getEngagementLetterId());
        if (engagementLetter.getClosingDate() != null) {
            throw new BadRequestException("Cannot accept engagement letter: engagement letter is closed");
        }
        if (engagementLetter.getAcceptanceEngagements() != null && !engagementLetter.getAcceptanceEngagements().isEmpty()) {
            throw new BadRequestException("Cannot accept engagement letter: engagement letter has already been accepted");
        }

        AcceptanceEngagement acceptanceEngagement = AcceptanceEngagement.builder()
                .signatureDate(LocalDateTime.now())
                .signer(Optional.ofNullable(publicAccessToken.getCustomerId())
                        .map(customerId -> UserDto.builder().id(customerId).build())
                        .orElse(null))
                .build();
        List<AcceptanceEngagement> acceptanceEngagements = new ArrayList<>(
                Optional.ofNullable(engagementLetter.getAcceptanceEngagements()).orElse(List.of())
        );
        acceptanceEngagements.add(acceptanceEngagement);
        engagementLetter.setAcceptanceEngagements(acceptanceEngagements);
        this.engagementLetterPersistence.update(engagementLetter.getId(), engagementLetter);

        int currentUsedCount = Optional.ofNullable(publicAccessToken.getUsedCount()).orElse(0);
        publicAccessToken.setUsedCount(currentUsedCount + 1);
        publicAccessToken.setIsActive(false);
        this.publicAccessTokenPersistence.update(publicAccessToken);
        return engagementLetter;
    }

    private PublicAccessToken validatePublicAccessToken(String token, String action) {
        PublicAccessToken publicAccessToken = this.publicAccessTokenPersistence.readByToken(token);
        if (!Boolean.TRUE.equals(publicAccessToken.getIsActive())) {
            throw new BadRequestException("Cannot " + action + " engagement letter: public access token is inactive");
        }
        if (publicAccessToken.getExpiresAt() != null && publicAccessToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot " + action + " engagement letter: public access token has expired");
        }
        int currentUsedCount = Optional.ofNullable(publicAccessToken.getUsedCount()).orElse(0);
        int maxUses = Optional.ofNullable(publicAccessToken.getMaxUses()).orElse(Integer.MAX_VALUE);
        if (currentUsedCount >= maxUses) {
            throw new BadRequestException("Cannot " + action + " engagement letter: public access token has exceeded its maximum uses");
        }
        if (publicAccessToken.getPurpose() != TokenPurpose.ACCEPT_ENGAGEMENT) {
            throw new BadRequestException("Cannot " + action + " engagement letter: public access token purpose is invalid");
        }
        return publicAccessToken;
    }

    public Stream<EngagementLetter> findNullSafe(EngagementLetterFindCriteria criteria) {
        if (criteria.getOwner() == null) {
            return this.engagementLetterPersistence.findNullSafe(criteria);
        } else {
            List<UUID> ids = this.userWebClient.findNullSafe(criteria.getOwner()).stream()
                    .map(UserDto::getId).toList();
            return this.engagementLetterPersistence.findNullSafe(criteria)
                    .filter(engagementLetter -> {
                        return ids.contains(engagementLetter.getOwner().getId());
                    });
        }
    }

    public byte[] generatePdf(UUID engagementLetterId) {
        EngagementLetter letter = this.readById(engagementLetterId);
        PdfTextRepository texts = new PdfTextRepository("templates/engagement-letter-texts.txt");

        Map<String, String> vars = Map.of(
                "cliente_nombre", letter.getOwner().getFirstName() + " " + letter.getOwner().getFamilyName(),
                "cliente_telefono", letter.getOwner().getMobile()
        );

        PdfBuilder pdf = new PdfBuilder("carta-encargo-" + engagementLetterId)
                .header()
                .space()
                .title("HOJA DE ENCARGO PROFESIONAL")
                .paragraphBold("En Madrid, a " + formatDateLong(letter.getCreationDate()), Element.ALIGN_RIGHT)
                .space()
                .paragraph(texts.get("cliente", vars));

        // Otros intervinientes
        if (letter.getAttachments() != null && !letter.getAttachments().isEmpty()) {
            pdf.space().paragraphBold("Otros intervinientes:");
            letter.getAttachments().forEach(att ->
                    pdf.paragraph("• " + att.getFirstName() + " " + att.getFamilyName() + " (Tel: " + att.getMobile() + ")")
            );
        }

        // Servicios contratados
        pdf.section("Servicios Contratados");
        for (LegalProcedure procedure : letter.getLegalProcedures()) {
            String budgetText = "Honorarios: " + formatBudget(procedure.getBudget()) +
                    (Boolean.TRUE.equals(procedure.getVatIncluded()) ? " (IVA incluido)" : " (+ IVA)");

            pdf.twoColumns(
                    left -> left.paragraphBold(procedure.getTitle()),
                    right -> right.paragraphBold(budgetText)
            );

            if (procedure.getLegalTasks() != null && !procedure.getLegalTasks().isEmpty()) {
                pdf.list(procedure.getLegalTasks());
            }
            pdf.space();
        }

        pdf.space().paragraphBold(texts.get("ejecucion_trabajos"));

        // Formas de pago
        pdf.space().section("Formas de Pago");
        letter.getPaymentMethods().stream()
                .map(pm -> pm.getPercentage() + "% - " + pm.getDescription())
                .forEach(pdf::paragraph);

        pdf.section("Datos Bancarios")
                .labelValue("Cuenta", "ES09 1465 0100 96 1707148504")
                .labelValue("Entidad", "ING")
                .labelValue("Titular", "Nuria Ocaña Pérez");

        pdf.section("Combinación de vías")
                .paragraph(texts.get("combinacion_vias"));

        pdf.section("Condiciones Generales")
                .paragraphs(texts.get("condiciones_generales"))
                .paragraphBold(texts.get("nota_solidaridad"))
                .space()
                .paragraph(texts.get("desavenencias"));

        pdf.space()
                .paragraphBold("Advertencias:")
                .numberedList(List.of(
                        texts.get("advertencia_1"),
                        texts.get("advertencia_2"),
                        texts.get("advertencia_3"),
                        texts.get("advertencia_4")
                ));

        pdf.section("Jurisdicción")
                .paragraph(texts.get("jurisdiccion"));

        pdf.space(2)
                .paragraphBold("AVISO IMPORTANTE")
                .paragraph(texts.get("aviso_importante"))
                .space()
                .paragraph(texts.get("firma"));

        pdf.space(5)
                .twoColumnSignature(vars.get("cliente_nombre"), "Nuría Ocaña Pérez")
                .footer();

        return pdf.build();
    }

    private String formatDateLong(LocalDate date) {
        if (date == null) return "-";
        String[] meses = {"enero", "febrero", "marzo", "abril", "mayo", "junio",
                "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
        return date.getDayOfMonth() + " de " + meses[date.getMonthValue() - 1] + " de " + date.getYear();
    }

    private String formatBudget(BigDecimal budget) {
        if (budget == null) return "-";
        return String.format("%.2f EUR", budget);
    }
}
