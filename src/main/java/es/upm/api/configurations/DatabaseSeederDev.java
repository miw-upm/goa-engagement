package es.upm.api.configurations;

import es.upm.api.adapter.out.legal.mongo.customerfiledownload.CustomerFileDownloadEntity;
import es.upm.api.adapter.out.legal.mongo.customerfiledownload.CustomerFileDownloadRepository;
import es.upm.api.adapter.out.legal.mongo.engagementletter.*;
import es.upm.api.adapter.out.legal.mongo.authorizationpurposetemplate.AuthorizationPurposeTemplateEntity;
import es.upm.api.adapter.out.legal.mongo.authorizationpurposetemplate.AuthorizationPurposeTemplateRepository;
import es.upm.api.adapter.out.legal.mongo.legalproceduretemplate.LegalProcedureTemplateEntity;
import es.upm.api.adapter.out.legal.mongo.legalproceduretemplate.LegalProcedureTemplateRepository;
import es.upm.api.adapter.out.legal.mongo.legaltask.LegalTaskEntity;
import es.upm.api.adapter.out.legal.mongo.legaltask.LegalTaskRepository;
import es.upm.miw.base64url.Base64UrlGenerator;
import es.upm.miw.device.DeviceInfo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class DatabaseSeederDev {

    public static final UUID ID_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    public static final UUID ID_1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");
    public static final UUID ID_2 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0002");
    public static final UUID ID_3 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0003");
    public static final UUID ID_4 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004");
    public static final UUID ID_5 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005");
    public static final UUID ID_6 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0006");
    public static final UUID ID_7 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0007");
    public static final UUID ID_8 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0008");
    public static final UUID ID_9 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0009");
    public static final UUID ID_10 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000a");
    public static final UUID ID_11 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000b");
    public static final UUID ID_12 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000c");
    public static final UUID ID_13 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000d");

    public static final UUID C_0 = ID_4;
    public static final UUID C_1 = ID_5;
    public static final UUID C_2 = ID_6;
    public static final UUID[] US = {C_0, C_1, C_2};
    // Compatibilidad temporal con tests existentes
    public static final UUID[] UUIDS = {ID_0, ID_1, ID_2, ID_3, ID_4, ID_5, ID_6, ID_7, ID_8, ID_9, ID_10, ID_11, ID_12, ID_13};
    private static final String LEGAL_CLAUSE = "Clausula especial legal!!!. Clausula especial legal!!!. Clausula especial legal!!!."
            + "Clausula especial legal!!!. Clausula especial legal!!!. Clausula especial legal!!!."
            + "Clausula especial legal!!!. Clausula especial legal!!!. Clausula especial legal!!!."
            + "Clausula especial legal!!!. Clausula especial legal!!!. Clausula especial legal!!!.";

    private final LegalTaskRepository legalTaskRepository;
    private final AuthorizationPurposeTemplateRepository authorizationPurposeTemplateRepository;
    private final LegalProcedureTemplateRepository legalProcedureTemplateRepository;
    private final EngagementLetterRepository engagementLetterRepository;
    private final CustomerFileDownloadRepository customerFileDownloadRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        this.deleteAllAndInitializeAndSeedDataBase();
    }

    public void deleteAllAndInitializeAndSeedDataBase() {
        this.deleteAllAndInitialize();
        this.seedDataBaseJava();
    }

    private void deleteAllAndInitialize() {
        this.customerFileDownloadRepository.deleteAll();
        this.engagementLetterRepository.deleteAll();
        this.legalProcedureTemplateRepository.deleteAll();
        this.authorizationPurposeTemplateRepository.deleteAll();
        this.legalTaskRepository.deleteAll();
        log.warn("------- Delete All -----------");
    }

    private void seedDataBaseJava() {
        log.warn("------- Initial Load from JAVA ---------------------------------------------------------------");

        LegalTaskEntity[] tasks = {
                new LegalTaskEntity(ID_0, "Estudio de antecedentes y documentación"),
                new LegalTaskEntity(ID_1, "Asesoramiento jurídico"),
                new LegalTaskEntity(ID_2, "Localización de personas"),
                new LegalTaskEntity(ID_3, "Negociación de la aceptación o renuncia con contrario"),
                new LegalTaskEntity(ID_4, "Tramitación notarial de la herencia"),
                new LegalTaskEntity(ID_5, "Liquidación del Impuesto de Sucesiones y Plusvalía Mortis causa"),
                new LegalTaskEntity(ID_6, "Redacción del cuaderno particional de la herencia ante el notario correspondiente"),
                new LegalTaskEntity(ID_7, "Liquidación de Impuesto de Sucesiones (prescrito)"),
                new LegalTaskEntity(ID_8, "Averiguación de los posibles pasivos (deuda) existente"),
                new LegalTaskEntity(ID_9, "Tramitación de los seguros"),
                new LegalTaskEntity(ID_10, "Redacción de la escritura de herencia y tramitación con la notaría correspondiente"),
                new LegalTaskEntity(ID_11, "Asistencia letrada en la notaría"),
                new LegalTaskEntity(ID_12, "Inscripción de los correspondientes bienes inmuebles en los Registros de la Propiedad"),
                new LegalTaskEntity(ID_13, "Tramitación de la venta de las viviendas de la herencia con la inmobiliaria"),
        };
        this.legalTaskRepository.saveAll(List.of(tasks));
        log.warn("        ------- tareas legales --------------------------------------------------------------------");

        AuthorizationPurposeTemplateEntity[] authorizationPurposes = {
                new AuthorizationPurposeTemplateEntity(ID_0, "Gestiones bancarias y de seguros"),
                new AuthorizationPurposeTemplateEntity(ID_1, "Actuaciones ante administraciones publicas"),
                new AuthorizationPurposeTemplateEntity(ID_2, "Representacion para tramites notariales"),
        };
        this.authorizationPurposeTemplateRepository.saveAll(List.of(authorizationPurposes));
        log.warn("        ------- plantillas de proposito de autorizacion --------------------------------------------");

        LegalProcedureTemplateEntity[] templates = {
                new LegalProcedureTemplateEntity(ID_0, "Procedimiento de herencia", new BigDecimal("2500"),
                        List.of(tasks[0], tasks[1], tasks[2], tasks[3], tasks[4], tasks[5], tasks[6])),
                new LegalProcedureTemplateEntity(ID_1, "División de Herencia", new BigDecimal("3000"),
                        List.of(tasks[0], tasks[1], tasks[7], tasks[8], tasks[9], tasks[10], tasks[11])),
                new LegalProcedureTemplateEntity(ID_2, "Herencia notarial", new BigDecimal("1000"),
                        List.of(tasks[0], tasks[1], tasks[12], tasks[13])),
                new LegalProcedureTemplateEntity(ID_3, "Procedimiento de ejecución hipotecaria", new BigDecimal("4000"),
                        List.of(tasks[0], tasks[1], tasks[13]))
        };
        this.legalProcedureTemplateRepository.saveAll(List.of(templates));
        log.warn("        ------- plantilla de procedimientos legales -----------------------------------------------");


        DeviceInfo device = DeviceInfo.builder().deviceType("Escritorio").ipAddress("83.52.10.24")
                .operatingSystem("Windows").browser("chrome").build();

        AcceptanceEngagementEntity[] acceptances = {
                AcceptanceEngagementEntity.builder()
                        .signatureAt(LocalDateTime.now().plusHours(1))
                        .signerId(C_0)
                        .signerFullName("c1 family-c1")
                        .signerIdentity("66666603E")
                        .mobile("666666000")
                        .signerEmail("c1@gmail.com")
                        .signatureToken(Base64UrlGenerator.token())
                        .deviceInfo(device)
                        .documentAccepted(true)
                        .build(),
                AcceptanceEngagementEntity.builder()
                        .signatureAt(LocalDateTime.now().plusHours(1))
                        .signerId(C_1)
                        .signerFullName("c2 family-c2")
                        .signerIdentity("66666604T")
                        .mobile("666666001")
                        .signerEmail("c2@gmail.com")
                        .signatureToken(Base64UrlGenerator.token())
                        .deviceInfo(device).build(),
        };

        LegalProcedureEntity[] procedimientos = {
                LegalProcedureEntity.builder().title(templates[0].getTitle())
                        .legalTasks(List.of(tasks[0].getTitle(), tasks[1].getTitle(), tasks[2].getTitle(),
                                tasks[3].getTitle(), tasks[4].getTitle(), tasks[5].getTitle(), tasks[6].getTitle()))
                        .budget(new BigDecimal("2500")).startDate(LocalDate.now().minusDays(2)).vatIncluded(false).build(),
                LegalProcedureEntity.builder().title(templates[1].getTitle())
                        .legalTasks(List.of(tasks[0].getTitle(), tasks[1].getTitle(), tasks[7].getTitle(),
                                tasks[8].getTitle(), tasks[9].getTitle(), tasks[10].getTitle(), tasks[11].getTitle()))
                        .budget(new BigDecimal("3000")).startDate(LocalDate.now()).vatIncluded(true).build(),
                LegalProcedureEntity.builder().title(templates[2].getTitle())
                        .legalTasks(List.of(tasks[0].getTitle(), tasks[1].getTitle(), tasks[12].getTitle(), tasks[13].getTitle()))
                        .budget(new BigDecimal("1000")).startDate(LocalDate.now()).vatIncluded(false).build(),
        };

        EngagementLetterEntity[] encargos = {
                EngagementLetterEntity.builder().id(ID_0)
                        .budgetOnly(true)
                        .discount(10).lastUpdatedDate(LocalDate.now().minusDays(5))
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Provisión de fondos").percentage("40%").build())
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Finalizado el procedimiento").percentage("60%").build())
                        .ownerId(C_0)
                        .attachmentId(C_1)
                        .legalClause(LEGAL_CLAUSE)
                        .legalProcedureEntities(List.of(procedimientos[0], procedimientos[2]))
                        .acceptanceEngagementEntities(List.of(acceptances[0]))
                        .build(),
                EngagementLetterEntity.builder().id(ID_1)
                        .budgetOnly(false)
                        .discount(20).lastUpdatedDate(LocalDate.now())
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Provisión de fondos").percentage("40%").build())
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Finalizado el procedimiento").percentage("60%").build())
                        .ownerId(C_0)
                        .attachmentId(C_1)
                        .legalProcedureEntities(List.of(procedimientos[1], procedimientos[2]))
                        .acceptanceEngagementEntities(List.of(acceptances[0], acceptances[1]))
                        .build(),
                EngagementLetterEntity.builder().id(ID_2)
                        .budgetOnly(false)
                        .discount(15)
                        .legalClause(LEGAL_CLAUSE)
                        .lastUpdatedDate(LocalDate.now())
                        .ownerId(C_1)
                        .attachmentId(C_2)
                        .paymentMethodEntity(PaymentMethodEntity.builder()
                                .description("A la firma de la carta de encargo")
                                .percentage("50%").build())
                        .paymentMethodEntity(PaymentMethodEntity.builder()
                                .description("A la finalización del procedimiento")
                                .percentage("50%").build())
                        .legalProcedureEntities(List.of(procedimientos[0], procedimientos[1], procedimientos[2]))
                        .acceptanceEngagementEntities(List.of(acceptances[1]))
                        .build(),
                EngagementLetterEntity.builder().id(ID_3)
                        .budgetOnly(false)
                        .discount(10)
                        .lastUpdatedDate(LocalDate.now().minusDays(30))
                        .closingDate(LocalDate.now().minusDays(5))  // CERRADO
                        .ownerId(C_0)
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Completo").percentage("100%").build())
                        .legalProcedureEntities(List.of(procedimientos[0]))
                        .build(),
                EngagementLetterEntity.builder().id(ID_4)
                        .budgetOnly(false)
                        .discount(20).lastUpdatedDate(LocalDate.now())
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Provisión de fondos").percentage("40%").build())
                        .ownerId(C_0)
                        .legalProcedureEntities(List.of(procedimientos[1], procedimientos[2]))
                        .acceptanceEngagementEntities(List.of(acceptances[0]))
                        .build(),


        };

        this.engagementLetterRepository.saveAll(List.of(encargos));
        log.warn("        ------- Hojas de encargo ------------------------------------------------------------------");

        CustomerFileDownloadEntity[] customerFileDownloads = {
                CustomerFileDownloadEntity.builder()
                        .id(ID_0)
                        .downloadedAt(LocalDateTime.now().minusHours(2))
                        .customerId(C_0)
                        .documentType("engagement-letter")
                        .documentId(ID_0)
                        .downloadToken(this.passwordEncoder.encode(Base64UrlGenerator.token()))
                        .build(),
                CustomerFileDownloadEntity.builder()
                        .id(ID_1)
                        .downloadedAt(LocalDateTime.now().minusHours(1))
                        .customerId(C_1)
                        .documentType("engagement-letter")
                        .documentId(ID_1)
                        .downloadToken(this.passwordEncoder.encode(Base64UrlGenerator.token()))
                        .build(),
                CustomerFileDownloadEntity.builder()
                        .id(ID_2)
                        .downloadedAt(LocalDateTime.now().minusMinutes(20))
                        .customerId(C_0)
                        .documentType("engagement-budget")
                        .documentId(ID_2)
                        .downloadToken(this.passwordEncoder.encode(Base64UrlGenerator.token()))
                        .build(),
        };

        this.customerFileDownloadRepository.saveAll(List.of(customerFileDownloads));
        log.warn("        ------- Descargas de documentos de clientes -----------------------------------------------");
    }

}
