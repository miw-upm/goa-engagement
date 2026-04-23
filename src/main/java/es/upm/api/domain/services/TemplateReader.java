package es.upm.api.domain.services;

import es.upm.miw.exception.BadGatewayException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TemplateReader {

    private TemplateReader() {
    }

    public static String read(String path) {
        try (InputStream is = TemplateReader.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new BadGatewayException("Template not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BadGatewayException("Error reading template: " + path);
        }
    }

    public static String replaceVariables(String content, Map<String, String> variables) {
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return content;
    }

    public static String render(String templatePath, Map<String, String> variables) {
        return replaceVariables(read(templatePath), variables);
    }
}
