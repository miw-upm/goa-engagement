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
    private final TareaLegalRepository tareaLegalRepository;
    private final ProcedimientoLegalRepository procedimientoLegalRepository;
    private final HojaEncargoRepository hojaEncargoRepository;

    public DatabaseSeederDev(TareaLegalRepository tareaLegalRepository, ProcedimientoLegalRepository procedimientoLegalRepository, HojaEncargoRepository hojaEncargoRepository) {
        this.tareaLegalRepository = tareaLegalRepository;
        this.procedimientoLegalRepository = procedimientoLegalRepository;
        this.hojaEncargoRepository = hojaEncargoRepository;
        this.deleteAllAndInitializeAndSeedDataBase();
    }

    public void deleteAllAndInitializeAndSeedDataBase() {
        this.deleteAllAndInitialize();
        this.seedDataBaseJava();
    }

    private void deleteAllAndInitialize() {
        this.hojaEncargoRepository.deleteAll();
        this.procedimientoLegalRepository.deleteAll();
        this.tareaLegalRepository.deleteAll();
        log.warn("------- Delete All -----------");
    }

    private void seedDataBaseJava() {
        log.warn("------- Initial Load from JAVA ---------------------------------------------------------------");
        TareaLegalEntity[] tareas = {
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0000"))
                        .titulo("Estudio de antecedentes y documentación.").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0001"))
                        .titulo("Asesoramiento jurídico.").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0002"))
                        .titulo("Localización de personas.").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0003"))
                        .titulo("Negociación de la aceptación o renuncia con contrario.").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0004"))
                        .titulo("Tramitación notarial de la herencia.").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0005"))
                        .titulo("Liquidación del Impuesto de Sucesiones y Plusvalía Mortis causa.").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0006"))
                        .titulo("Redacción del cuaderno particional de la herencia ante el notario correspondiente.").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0007"))
                        .titulo("Liquidación de Impuesto de Sucesiones (prescrito).").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0008"))
                        .titulo("Averiguación de los posibles pasivos (deuda) existente.").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0009"))
                        .titulo("Tramitación de los seguros.").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff000a"))
                        .titulo("Estudio de antecedentes y documentación.").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa1-bbbb-cccc-dddd-eeeeffff000b"))
                        .titulo("Redacción de la escritura de herencia y tramitación con la notaría correspondiente.")
                        .build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa2-bbbb-cccc-dddd-eeeeffff000c"))
                        .titulo("Asistencia letrada en la notaría.").build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa3-bbbb-cccc-dddd-eeeeffff000d"))
                        .titulo("Inscripción de los correspondientes bienes inmuebles en los Registros de la Propiedad.")
                        .build(),
                TareaLegalEntity.builder().id(UUID.fromString("aaaaaaa3-bbbb-cccc-dddd-eeeeffff000e"))
                        .titulo("Tramitación de la venta de las viviendas de la herencia con la inmobiliaria.")
                        .build()
        };
        this.tareaLegalRepository.saveAll(List.of(tareas));
        log.warn("        ------- tareas legales --------------------------------------------------------------------");

        ProcedimientoLegalEntity[] procedimientos = {
                ProcedimientoLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0000"))
                        .titulo("Procedimiento de herencia.")
                        .tareaLegalEntities(List.of(tareas[0], tareas[1], tareas[2], tareas[3], tareas[4], tareas[5], tareas[6]))
                        .presupuesto(new BigDecimal("2500")).finalizado(false).ivaIncluido(false).build(),
                ProcedimientoLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0001"))
                        .titulo("División de Herencia.")
                        .tareaLegalEntities(List.of(tareas[0], tareas[1], tareas[7], tareas[8], tareas[9], tareas[10], tareas[11]))
                        .presupuesto(new BigDecimal("3000")).finalizado(false).ivaIncluido(true).build(),
                ProcedimientoLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0002"))
                        .titulo("Herencia notarial.")
                        .tareaLegalEntities(List.of(tareas[0], tareas[1], tareas[12], tareas[13]))
                        .presupuesto(new BigDecimal("1000")).finalizado(false).ivaIncluido(false).build(),
                ProcedimientoLegalEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0003"))
                        .titulo("Procedimiento de ejecución hipotecaria.")
                        .tareaLegalEntities(List.of(tareas[0], tareas[1], tareas[14]))
                        .presupuesto(new BigDecimal("4000")).finalizado(false).ivaIncluido(false).build(),
        };
        this.procedimientoLegalRepository.saveAll(List.of(procedimientos));
        log.warn("        ------- procedimientos legales ------------------------------------------------------------");

        HojaEncargoEntity[] encargos = {
                HojaEncargoEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0000"))
                        .descuento(10).fechaCreacion(LocalDate.now()).abierta(true)
                        .formaPago(FormaPago.builder().descripcion("Provisión de fondos").porcentaje(40).build())
                        .formaPago(FormaPago.builder().descripcion("Finalizado el procedimiento").porcentaje(60).build())
                        .propietarioId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004"))
                        .adjuntoId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005"))
                        .documentosAceptacion(DocumentoAceptacion.builder()
                                .fechaHorafirma(LocalDateTime.now()).justificante("link de justificante").build())
                        .procedimientoLegales(List.of(procedimientos[0])).build(),
                HojaEncargoEntity.builder().id(UUID.fromString("aaaaaaa0-bbbb-cccc-dddd-eeeeffff0001"))
                        .descuento(20).fechaCreacion(LocalDate.now()).abierta(false)
                        .formaPago(FormaPago.builder().descripcion("Provisión de fondos").porcentaje(40).build())
                        .formaPago(FormaPago.builder().descripcion("Finalizado el procedimiento").porcentaje(60).build())
                        .propietarioId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004"))
                        .adjuntoId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005"))
                        .documentosAceptacion(DocumentoAceptacion.builder()
                                .fechaHorafirma(LocalDateTime.now()).justificante("link de justificante").build())
                        .procedimientoLegales(List.of(procedimientos[1], procedimientos[2])).build(),
        };
        this.hojaEncargoRepository.saveAll(List.of(encargos));
        log.warn("        ------- Hojas de encargo ------------------------------------------------------------------");
    }

}
