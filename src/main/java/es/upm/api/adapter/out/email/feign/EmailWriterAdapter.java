package es.upm.api.adapter.out.email.feign;

import es.upm.api.domain.ports.out.email.EmailWriter;
import es.upm.miw.mail.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailWriterAdapter implements EmailWriter {
    private final GoaSupportClient goaSupportClient;

    @Override
    public void sendHtml(Email email) {
        this.goaSupportClient.sendHtml(email);
    }
}
