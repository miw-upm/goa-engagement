package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.TicketEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
class TicketRepositoryIT {

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void testFindByUrlToken() {
        Optional<TicketEntity> ticket = this.ticketRepository.findByUrlToken("AAAABBBBCCCCDDDDEEEE00");
        assertThat(ticket).isPresent();
        assertThat(ticket.get().getId()).isEqualTo(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000"));
    }

}
