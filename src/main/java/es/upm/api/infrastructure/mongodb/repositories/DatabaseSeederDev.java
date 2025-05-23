package es.upm.api.infrastructure.mongodb.repositories;


import es.upm.api.domain.model.LineState;
import es.upm.api.infrastructure.mongodb.entities.TicketEntity;
import es.upm.api.infrastructure.mongodb.entities.TicketLineEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;

@Log4j2
@Service
@Profile({"dev", "test"})
public class DatabaseSeederDev {
    private final TicketRepository ticketRepository;

    public DatabaseSeederDev(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
        this.deleteAllAndInitializeAndSeedDataBase();
    }

    public void deleteAllAndInitializeAndSeedDataBase() {
        this.deleteAllAndInitialize();
        this.seedDataBaseJava();
    }

    private void deleteAllAndInitialize() {
        this.ticketRepository.deleteAll();
        log.warn("------- Delete All -----------");
    }

    private void seedDataBaseJava() {
        log.warn("------- Initial Load from JAVA ---------------------------------------------------------------");
        TicketLineEntity[] ticketLineEntities = {
                TicketLineEntity.builder().articleId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000"))
                        .retailPrice(BigDecimal.TEN).amount(1).discount(ZERO).state(LineState.COMMITTED).build(),
                TicketLineEntity.builder().articleId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001"))
                        .retailPrice(new BigDecimal("16.8")).amount(1).discount(new BigDecimal("50"))
                        .state(LineState.NOT_COMMITTED).build(),
        };
        TicketEntity[] tickets = {
                TicketEntity.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000"))
                        .urlToken("AAAABBBBCCCCDDDDEEEE00").creationDate(LocalDateTime.now())
                        .cash(new BigDecimal("18.4")).note("test")
                        .ticketLineEntities(List.of(ticketLineEntities[0], ticketLineEntities[1]))
                        .userId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000")).build()
        };
        this.ticketRepository.saveAll(List.of(tickets));
        log.warn("        ------- tickets --------------------------------------------------------------------");
    }

}
