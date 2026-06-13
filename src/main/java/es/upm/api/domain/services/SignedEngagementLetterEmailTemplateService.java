package es.upm.api.domain.services;

import es.upm.miw.mail.Email;
import es.upm.miw.mail.EmailTemplateRenderer;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SignedEngagementLetterEmailTemplateService {
    private static final String SUBJECT = "Firmado Hoja de Encargo";
    private static final String HTML_TEMPLATE_PATH = "templates/signed-engagement-letter.html";

    public Email buildHtmlEmail(String to, String firstName) {
        String body = EmailTemplateRenderer.render(HTML_TEMPLATE_PATH, Map.of("FIRST_NAME", firstName));
        return Email.builder().to(to).subject(SUBJECT).body(body).build();
    }
}
