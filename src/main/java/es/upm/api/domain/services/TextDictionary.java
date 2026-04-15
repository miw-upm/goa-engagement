package es.upm.api.domain.services;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextDictionary {

    private static final Pattern BLOCK_PATTERN = Pattern.compile("^#([a-z_0-9]+)$", Pattern.MULTILINE);

    private final Map<String, String> texts = new HashMap<>();

    public TextDictionary(String templatePath) {
        parse(TemplateReader.read(templatePath));
    }

    private void parse(String content) {
        Matcher matcher = BLOCK_PATTERN.matcher(content);
        int lastStart = -1;
        String lastId = null;

        while (matcher.find()) {
            if (lastId != null) {
                texts.put(lastId, content.substring(lastStart, matcher.start()).trim());
            }
            lastId = matcher.group(1);
            lastStart = matcher.end() + 1;
        }

        if (lastId != null && lastStart < content.length()) {
            texts.put(lastId, content.substring(lastStart).trim());
        }
    }

    public String get(String id) {
        String text = texts.getOrDefault(id, "");
        return normalize(text);
    }

    public String get(String id, Map<String, String> variables) {
        return TemplateReader.replaceVariables(get(id), variables);
    }

    private String normalize(String text) {
        // Normalizar saltos de línea Windows
        text = text.replace("\r\n", "\n");
        // Preservar dobles saltos (separadores de párrafo)
        text = text.replace("\n\n", "{{BREAK}}");
        // Saltos simples -> espacio
        text = text.replace("\n", " ");
        // Restaurar dobles saltos
        text = text.replace("{{BREAK}}", "\n\n");
        // Limpiar espacios múltiples
        text = text.replaceAll(" +", " ");
        return text.trim();
    }
}
