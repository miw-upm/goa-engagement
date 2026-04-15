package es.upm.api.domain.services;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextDictionary {

    private static final Pattern BLOCK_PATTERN = Pattern.compile("^#([a-z_0-9]+):(.*)$", Pattern.MULTILINE);

    private final Map<String, String> titles = new HashMap<>();
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
                texts.put(lastId, normalize(content.substring(lastStart, matcher.start()).trim()));
            }
            lastId = matcher.group(1);
            String title = matcher.group(2).trim();
            titles.put(lastId, title.isEmpty() ? null : title);
            lastStart = matcher.end() + 1;
        }

        if (lastId != null && lastStart < content.length()) {
            texts.put(lastId, normalize(content.substring(lastStart).trim()));
        }
    }

    public String getTitle(String id) {
        return titles.get(id);
    }

    public String getText(String id) {
        return texts.getOrDefault(id, "");
    }

    public String getText(String id, Map<String, String> variables) {
        return TemplateReader.replaceVariables(getText(id), variables);
    }

    public boolean hasTitle(String id) {
        return titles.get(id) != null;
    }

    private String normalize(String text) {
        text = text.replace("\r\n", "\n");
        text = text.replace("\n\n", "{{BREAK}}");
        text = text.replace("\n", " ");
        text = text.replace("{{BREAK}}", "\n\n");
        text = text.replaceAll(" +", " ");
        return text.trim();
    }
}