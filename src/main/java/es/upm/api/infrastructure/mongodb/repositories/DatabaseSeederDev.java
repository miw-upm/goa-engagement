package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@Profile({"dev", "test"})
public class DatabaseSeederDev {
    private final LegalTaskRepository legalTaskRepository;
    private final LegalProcedureRepository legalProcedureRepository;
    private final EngagementLetterRepository engagementLetterRepository;

    public DatabaseSeederDev(LegalTaskRepository legalTaskRepository, LegalProcedureRepository legalProcedureRepository, EngagementLetterRepository engagementLetterRepository) {
        this.legalTaskRepository = legalTaskRepository;
        this.legalProcedureRepository = legalProcedureRepository;
        this.engagementLetterRepository = engagementLetterRepository;
        this.deleteAllAndInitializeAndSeedDataBase();
    }

    public void deleteAllAndInitializeAndSeedDataBase() {
        this.deleteAllAndInitialize();
        this.seedDataBaseJava();
    }

    private void deleteAllAndInitialize() {
        this.engagementLetterRepository.deleteAll();
        this.legalProcedureRepository.deleteAll();
        this.legalTaskRepository.deleteAll();
        log.warn("------- Delete All -----------");
    }

    private void seedDataBaseJava() {
        log.warn("------- Initial Load from JAVA ---------------------------------------------------------------");
        LegalTaskEntity[] tareas = {
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0000"))
                        .title("Estudio de antecedentes y documentación").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0001"))
                        .title("Asesoramiento jurídico").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0002"))
                        .title("Localización de personas").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0003"))
                        .title("Negociación de la aceptación o renuncia con contrario").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0004"))
                        .title("Tramitación notarial de la herencia").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0005"))
                        .title("Liquidación del Impuesto de Sucesiones y Plusvalía Mortis causa").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0006"))
                        .title("Redacción del cuaderno particional de la herencia ante el notario correspondiente").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0007"))
                        .title("Liquidación de Impuesto de Sucesiones (prescrito)").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0008"))
                        .title("Averiguación de los posibles pasivos (deuda) existente").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0009"))
                        .title("Tramitación de los seguros").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff000a"))
                        .title("Estudio de antecedentes y documentación").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa1-bbbb-cccc-dddd-eeeeffff000b"))
                        .title("Redacción de la escritura de herencia y tramitación con la notaría correspondiente")
                        .build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa2-bbbb-cccc-dddd-eeeeffff000c"))
                        .title("Asistencia letrada en la notaría").build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa3-bbbb-cccc-dddd-eeeeffff000d"))
                        .title("Inscripción de los correspondientes bienes inmuebles en los Registros de la Propiedad")
                        .build(),
                LegalTaskEntity.builder().id(UUID.fromString("aaaaaaa3-bbbb-cccc-dddd-eeeeffff000e"))
                        .title("Tramitación de la venta de las viviendas de la herencia con la inmobiliaria")
                        .build()
        };
        this.legalTaskRepository.saveAll(List.of(tareas));
        log.warn("        ------- tareas legales --------------------------------------------------------------------");

        LegalProcedureTemplateEntity[] plantillaProcedimientos = {
                LegalProcedureTemplateEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0000"))
                        .title("Procedimiento de herencia")
                        .legalTaskEntities(List.of(tareas[0], tareas[1], tareas[2], tareas[3], tareas[4], tareas[5], tareas[6]))
                        .budget(new BigDecimal("2500")).build(),
                LegalProcedureTemplateEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0001"))
                        .title("División de Herencia")
                        .legalTaskEntities(List.of(tareas[0], tareas[1], tareas[7], tareas[8], tareas[9], tareas[10], tareas[11]))
                        .budget(new BigDecimal("3000")).build(),
                LegalProcedureTemplateEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0002"))
                        .title("Herencia notarial").legalTaskEntities(List.of(tareas[0], tareas[1], tareas[12], tareas[13]))
                        .budget(new BigDecimal("1000")).build(),
                LegalProcedureTemplateEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0003"))
                        .title("Procedimiento de ejecución hipotecaria").legalTaskEntities(List.of(tareas[0], tareas[1], tareas[14]))
                        .budget(new BigDecimal("4000")).build(),
        };
        this.legalProcedureRepository.saveAll(List.of(plantillaProcedimientos));
        log.warn("        ------- plantilla de procedimientos legales -----------------------------------------------");

        LegalProcedureEntity[] procedimientos = {
                LegalProcedureEntity.builder().title(plantillaProcedimientos[0].getTitle())
                        .legalTasks(List.of(tareas[0].getTitle(), tareas[1].getTitle(), tareas[2].getTitle(),
                                tareas[3].getTitle(), tareas[4].getTitle(), tareas[5].getTitle(), tareas[6].getTitle()))
                        .budget(new BigDecimal("2500")).startDate(LocalDate.now().minusDays(2)).vatIncluded(false).build(),
                LegalProcedureEntity.builder().title(plantillaProcedimientos[1].getTitle())
                        .legalTasks(List.of(tareas[0].getTitle(), tareas[1].getTitle(), tareas[7].getTitle(),
                                tareas[8].getTitle(), tareas[9].getTitle(), tareas[10].getTitle(), tareas[11].getTitle()))
                        .budget(new BigDecimal("3000")).startDate(LocalDate.now()).vatIncluded(true).build(),
                LegalProcedureEntity.builder().title(plantillaProcedimientos[2].getTitle())
                        .legalTasks(List.of(tareas[0].getTitle(), tareas[1].getTitle(), tareas[12].getTitle(), tareas[13].getTitle()))
                        .budget(new BigDecimal("1000")).startDate(LocalDate.now()).vatIncluded(false).build(),
        };

        EngagementLetterEntity[] encargos = {
                EngagementLetterEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0000"))
                        .discount(10).creationDate(LocalDate.now().minusDays(5))
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Provisión de fondos").percentage(40).build())
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Finalizado el procedimiento").percentage(60).build())
                        .ownerId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004"))
                        .attachmentId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005"))
                        .acceptanceDocumentEntity(AcceptanceDocumentEntity.builder()
                                .signatureDate(LocalDateTime.now()).receipt("link de justificante").build())
                        .legalProcedureEntities(List.of(procedimientos[0])).build(),
                EngagementLetterEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0001"))
                        .discount(20).creationDate(LocalDate.now())
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Provisión de fondos").percentage(40).build())
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Finalizado el procedimiento").percentage(60).build())
                        .ownerId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004"))
                        .attachmentId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005"))
                        .acceptanceDocumentEntity(AcceptanceDocumentEntity.builder()
                                .signatureDate(LocalDateTime.now()).receipt("link de justificante").build())
                        .legalProcedureEntities(List.of(procedimientos[1], procedimientos[2])).build(),
        };
        this.engagementLetterRepository.saveAll(List.of(encargos));
        log.warn("        ------- Hojas de encargo ------------------------------------------------------------------");
    }

}
