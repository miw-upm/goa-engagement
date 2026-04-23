package es.upm.api.domain.services;

import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextDictionary {

    private static final Entry EMPTY_ENTRY = new Entry(null, null, List.of());

    private final Map<String, Entry> entries = new HashMap<>();

    public TextDictionary(String templatePath) {
        this.parseYaml(TemplateReader.read(templatePath));
    }

    private void parseYaml(String content) {
        Object parsed = new Yaml().load(content);
        if (!(parsed instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            this.entries.put(key, this.parseEntry(entry.getValue()));
        }
    }

    private Entry parseEntry(Object value) {
        if (value instanceof String textValue) {
            return new Entry(null, this.normalize(textValue), List.of());
        }

        if (!(value instanceof Map<?, ?> node)) {
            return new Entry(null, null, List.of());
        }

        String title = this.normalizeNullable(node.get("title"));
        String text = this.normalizeNullable(node.get("text"));
        List<String> list = this.normalizeList(node.get("list"));
        return new Entry(title, text, list);
    }

    private String normalizeNullable(Object value) {
        if (value == null) {
            return null;
        }
        String normalized = this.normalize(String.valueOf(value));
        return normalized.isBlank() ? null : normalized;
    }

    private List<String> normalizeList(Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        List<String> normalized = new java.util.ArrayList<>();
        for (Object item : values) {
            if (item == null) {
                continue;
            }
            String normalizedValue = this.normalize(String.valueOf(item));
            if (!normalizedValue.isBlank()) {
                normalized.add(normalizedValue);
            }
        }
        return normalized;
    }

    public String getTitle(String id) {
        return this.getEntry(id).title();
    }

    public String getText(String id) {
        Entry entry = this.getEntry(id);
        return entry.text() == null ? "" : entry.text();
    }

    public String getText(String id, Map<String, String> variables) {
        return TemplateReader.replaceVariables(getText(id), variables);
    }

    public List<String> getList(String id) {
        return this.getEntry(id).list();
    }

    private Entry getEntry(String id) {
        return this.entries.getOrDefault(id, EMPTY_ENTRY);
    }

    private String normalize(String text) {
        text = text.replace("\r\n", "\n");
        text = text.replace("\n\n", "{{BREAK}}");
        text = text.replace("\n", " ");
        text = text.replace("{{BREAK}}", "\n\n");
        text = text.replaceAll(" +", " ");
        return text.trim();
    }

    private record Entry(String title, String text, List<String> list) {
    }
}
