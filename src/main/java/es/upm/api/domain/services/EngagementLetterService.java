package es.upm.api.domain.services;

import es.upm.api.domain.exceptions.BadRequestException;
import es.upm.api.domain.model.*;
import es.upm.api.domain.persistence.EngagementLetterPersistence;
import es.upm.api.domain.persistence.PublicAccessTokenPersistence;
import es.upm.api.domain.webclients.UserWebClient;
import es.upm.miw.pdf.PdfBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
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
                    .filter(engagementLetter -> {
                        return ids.contains(engagementLetter.getOwner().getId());
                    });
        }
    }

    public byte[] generatePdf(UUID engagementLetterId) {
        EngagementLetter letter = this.readById(engagementLetterId);

        PdfBuilder pdf = new PdfBuilder("carta-encargo-" + engagementLetterId)
                .header()
                .title("HOJA DE ENCARGO PROFESIONAL");

        // Lugar y fecha
        pdf.paragraph("En Madrid, a " + formatDateLong(letter.getCreationDate()))
                .space();

        // Párrafo introductorio
        String introText = String.format(
                "D./Dña. %s %s, provisto de número de teléfono %s, en su nombre y derecho, " +
                        "encarga profesionalmente a la abogada Nuria Ocaña Pérez con número de D.N.I. n.º 46.882.956-D " +
                        "y número de colegiada 135.280 del ICAM con despacho profesional sito en el Paseo de la Castellana, " +
                        "93 – 2º planta de Madrid la realización de los siguientes trabajos profesionales:",
                letter.getOwner().getFirstName(),
                letter.getOwner().getFamilyName(),
                letter.getOwner().getMobile()
        );
        pdf.paragraph(introText);

        // Otros intervinientes
        if (letter.getAttachments() != null && !letter.getAttachments().isEmpty()) {
            pdf.space()
                    .paragraphBold("Otros intervinientes:");
            letter.getAttachments().forEach(att ->
                    pdf.paragraph("• " + att.getFirstName() + " " + att.getFamilyName() + " (Tel: " + att.getMobile() + ")")
            );
        }

        // Procedimientos legales
        pdf.section("Servicios Contratados");
        for (LegalProcedure procedure : letter.getLegalProcedures()) {
            pdf.space()
                    .paragraphBold(procedure.getTitle());

            if (procedure.getLegalTasks() != null && !procedure.getLegalTasks().isEmpty()) {
                pdf.list(procedure.getLegalTasks());
            }

            String budgetText = formatBudget(procedure.getBudget());
            if (Boolean.TRUE.equals(procedure.getVatIncluded())) {
                budgetText += " (IVA incluido)";
            } else {
                budgetText += " (+ IVA)";
            }
            pdf.labelValue("Honorarios", budgetText);
        }

        pdf.space()
                .paragraphBold("La ejecución de estos trabajos profesionales se efectuará en régimen de arrendamiento " +
                        "de servicios, con arreglo a las normas deontológicas de la Abogacía. En consecuencia, con carácter " +
                        "indicativo y al margen de las incidencias que puedan plantearse incluyendo los honorarios de otros " +
                        "profesionales que deban intervenir (procurador, peritos, costas de contrario, Notarios, Registros, etc.), " +
                        "no incluirán los gastos de desplazamiento o de otra naturaleza, ni suplidos que puedan ocasionarse " +
                        "en la ejecución de los trabajos objeto de este encargo.");

        pdf.section("Formas de Pago");
        List<String> paymentDescriptions = letter.getPaymentMethods().stream()
                .map(pm -> pm.getPercentage() + "% - " + pm.getDescription())
                .toList();
        pdf.list(paymentDescriptions);

        pdf.section("Datos Bancarios")
                .labelValue("Cuenta", "ES09 1465 0100 96 1707148504")
                .labelValue("Entidad", "ING")
                .labelValue("Titular", "Nuria Ocaña Pérez");

        pdf.section("Combinación de vías")
                .paragraph("Existe la posibilidad de que una fase (por ejemplo, la formación de inventario) se tramite " +
                        "de forma contenciosa, y la posterior liquidación se realice ante notario de común acuerdo. " +
                        "Por ello, se ha optado por desglosar los honorarios por fases y vías, para facilitar la " +
                        "adaptación del presupuesto al desarrollo real del procedimiento.");

        // Cláusulas legales
        pdf.section("Condiciones Generales")
                .paragraph("El cliente autoriza expresamente a Ocaña Abogados para actuar en su nombre y representación en los procedimientos indicados.")
                .space()
                .paragraph("Los honorarios indicados corresponden exclusivamente a los servicios descritos. Cualquier actuación adicional será presupuestada por separado.");

        pdf.section("Condiciones Generales")
                .paragraph("Los honorarios pactados en la presente hoja de encargo profesional incluyen las consultas " +
                        "relacionadas con el encargo que tengan lugar durante los dos meses posteriores a la resolución " +
                        "o documento que ponga fin al procedimiento. Transcurrido dicho tiempo, las consultas que surjan " +
                        "relacionadas con el encargo y la aplicación de la resolución tendrán un coste de 150,00 € + IVA, " +
                        "que serán abonadas en el momento de efectuar la consulta.")
                .space()
                .paragraph("Los honorarios de Letrado objeto de la presente se presupuestan al margen de incidencias y recursos " +
                        "que puedan plantearse y sin incluir los honorarios de otros profesionales que puedan o deban intervenir, " +
                        "como procuradores o peritos. El pago de las tasas y depósitos judiciales que pudieran existir, serán de " +
                        "cuenta del cliente y no se encuentran incluidos en los honorarios descritos. No se incluyen gastos por " +
                        "desplazamientos, alojamiento, dietas o gastos de otra naturaleza (gastos de correos; envío de burofaxes; " +
                        "notas y certificaciones registrales, peticiones de cuentas al Registro de la Propiedad...), ni suplidos " +
                        "que puedan ocasionarse en la ejecución de los trabajos objeto de este encargo. Los gastos y suplidos en " +
                        "que se incurran serán facturados según se vayan produciendo.")
                .space()
                .paragraph("Los honorarios pactados únicamente cubrirán las actuaciones realizadas en la Comunidad de Madrid. " +
                        "De igual modo, cualquier servicio jurídico que no esté incluido en el presente contrato se realizará " +
                        "previo presupuesto y aceptación por ambas partes.")
                .space()
                .paragraph("La minuta de honorarios definitiva estará sujeta al régimen fiscal de retenciones e IVA procedentes " +
                        "que serán abonadas por el cliente.")
                .space()
                .paragraphBold("Nota: de ser varias las personas que realizan el encargo, esa obligación será asumida con " +
                        "carácter solidario por todas ellas.")
                .space()
                .paragraph("En el caso de que existieran desavenencias con relación al asunto encomendado, cualquiera de las " +
                        "partes podrá desistir unilateralmente, previo aviso por escrito a la otra parte, de la continuación " +
                        "en la prestación de los servicios por parte de la Letrada, debiéndose liquidar en ese momento las " +
                        "cantidades adeudadas por el cliente por los trabajos efectivamente realizados por la Letrada.");

        pdf.space()
                .paragraphBold("Advertencias:")
                .numberedList(List.of(
                        "El ejercicio de la acción puede ser infructuoso.",
                        "Los Letrados se encuentran sujetos a las normas sobre prevención de blanqueo de capitales y " +
                                "financiación del terrorismo establecidas en la Ley 10/2010.",
                        "El cliente autoriza a entregar copia de la documentación facilitada a terceros necesarios para " +
                                "la realización del encargo.",
                        "La Letrada podrá delegar tareas en los Abogados colaboradores de su despacho."
                ));

        pdf.section("Jurisdicción")
                .paragraph("Las partes se someten expresamente a la legislación española, sometiéndose a la jurisdicción " +
                        "de los Juzgados y Tribunales de Madrid.");

        pdf.space()
                .paragraphBold("AVISO IMPORTANTE")
                .paragraph("La validez de la presente hoja de encargo profesional es de 30 días naturales a contar desde " +
                        "su emisión. Cualquier modificación sustancial en las actuaciones del procedimiento serán tenidas " +
                        "en cuenta para una valoración de liquidación final, así como la existencia de nuevas actuaciones " +
                        "o procedimientos judiciales no contempladas en el presente presupuesto serán origen de solicitud " +
                        "por parte del despacho de una nueva provisión de fondos.")
                .space()
                .paragraph("En virtud de lo expuesto y en prueba de conformidad, se firma el presente encargo por duplicado " +
                        "y a un solo efecto, en el lugar y fecha arriba indicada.");

        // Firmas
        pdf.space()
                .twoColumnSignature("El Cliente", "El Abogado")
                .footer();

        return pdf.build();
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "-";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String formatBudget(BigDecimal budget) {
        if (budget == null) return "-";
        return NumberFormat.getCurrencyInstance(new Locale("es", "ES")).format(budget);
    }

    private String formatPaymentMethod(PaymentMethod method) {
        // Adapta según tu enum/clase PaymentMethod
        return method.toString();
    }

    private String formatDateLong(LocalDate date) {
        if (date == null) return "-";
        String[] meses = {"enero", "febrero", "marzo", "abril", "mayo", "junio",
                "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
        return date.getDayOfMonth() + " de " + meses[date.getMonthValue() - 1] + " de " + date.getYear();
    }
}
