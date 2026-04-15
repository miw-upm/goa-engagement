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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                    .filter(engagementLetter -> ids.contains(engagementLetter.getOwner().getId()));
        }
    }

    public byte[] generatePdf(UUID engagementLetterId) {
        EngagementLetter letter = this.readById(engagementLetterId);
        TextDictionary texts = new TextDictionary("templates/engagement-letter-texts.txt");

        Map<String, Object> dict = new HashMap<>();
        dict.put("fecha", letter.getCreationDate()
                .format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "ES"))));
        dict.put("intervinientes", texts.get("intervinientes", Map.of("clientes", letter.buildClientsFullNameIdentity())));
        dict.put("services", letter.getLegalProcedures().stream()
                .map(procedure -> Map.of(
                        "title", procedure.getTitle(),
                        "budget", procedure.buildFormatBudget() + (Boolean.TRUE.equals(procedure.getVatIncluded()) ? " (IVA incluido)" : " (+ IVA)"),
                        "tasks", procedure.getLegalTasks() != null ? procedure.getLegalTasks() : List.<String>of()
                ))
                .toList());
        dict.put("ejecucion", texts.get("ejecucion_trabajos"));
        dict.put("pagos", letter.getPaymentMethods().stream()
                .map(pm -> pm.getPercentage() + "% - " + pm.getDescription())
                .toList());
        dict.put("combinacion", texts.get("combinacion_vias"));
        dict.put("condiciones", texts.get("condiciones_generales"));
        dict.put("solidaridad", texts.get("nota_solidaridad"));
        dict.put("desavenencias", texts.get("desavenencias"));
        dict.put("advertencias", List.of(
                texts.get("advertencia_1"),
                texts.get("advertencia_2"),
                texts.get("advertencia_3"),
                texts.get("advertencia_4"),
                texts.get("advertencia_5")));
        dict.put("seguro", texts.get("seguro_rc"));
        dict.put("comunicaciones", texts.get("comunicaciones"));
        dict.put("proteccion", texts.get("proteccion_datos"));
        dict.put("jurisdiccion", texts.get("jurisdiccion"));
        dict.put("aviso", texts.get("aviso_importante"));
        dict.put("firma", texts.get("firma"));
        dict.put("clientsNames", letter.buildClientsName());

        PdfBuilder pdf = new PdfBuilder()
                .header()
                .space()
                .title("HOJA DE ENCARGO PROFESIONAL")
                .paragraphBold("En Madrid, a " + dict.get("fecha"), Element.ALIGN_RIGHT)
                .space()
                .paragraph((String) dict.get("intervinientes"))
                .section("Servicios Contratados");

        for (Map<String, Object> service : (List<Map<String, Object>>) dict.get("services")) {
            String title = (String) service.get("title");
            String budget = (String) service.get("budget");
            List<String> tasks = (List<String>) service.get("tasks");

            pdf.twoColumns(
                    left -> left.paragraphBold(title),
                    right -> right.paragraphBold(budget, Element.ALIGN_RIGHT)
            );
            if (!tasks.isEmpty()) {
                pdf.list(tasks);
            }
            pdf.space();
        }

        pdf.space()
                .paragraphBold((String) dict.get("ejecucion"))
                .space()
                .section("Formas de Pago");

        ((List<String>) dict.get("pagos")).forEach(pdf::paragraph);

        pdf.section("Datos Bancarios")
                .labelValue("Cuenta", "ES09 1465 0100 96 1707148504")
                .labelValue("Entidad", "ING")
                .labelValue("Titular", "Nuria Ocaña Pérez")
                .section("Combinación de vías")
                .paragraph((String) dict.get("combinacion"))
                .section("Condiciones Generales")
                .paragraphs((String) dict.get("condiciones"))
                .paragraphBold((String) dict.get("solidaridad"))
                .space()
                .paragraph((String) dict.get("desavenencias"))
                .space()
                .paragraphBold("Advertencias:")
                .numberedList((List<String>) dict.get("advertencias"))
                .section("Seguro de Responsabilidad Civil")
                .paragraph((String) dict.get("seguro"))
                .section("Comunicaciones")
                .paragraph((String) dict.get("comunicaciones"))
                .section("Protección de Datos")
                .paragraph((String) dict.get("proteccion"))
                .section("Jurisdicción")
                .paragraph((String) dict.get("jurisdiccion"))
                .space(3)
                .paragraphBold("AVISO IMPORTANTE")
                .paragraph((String) dict.get("aviso"))
                .space()
                .paragraph((String) dict.get("firma"))
                .space(2)
                .multiSignature((List<String>) dict.get("clientsNames"), "Nuria Ocaña Pérez")
                .footer();

        return pdf.build();
    }

}
