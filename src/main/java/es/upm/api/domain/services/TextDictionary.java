package es.upm.api.domain.services;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextDictionary {

    private static final Pattern BLOCK_PATTERN = Pattern.compile("^#([a-z_0-9]+):(.*)$", Pattern.MULTILINE);
    private static final Pattern LIST_PATTERN = Pattern.compile("^#([a-z_0-9]+)\\*:(.*)$", Pattern.MULTILINE);

    private final Map<String, String> titles = new HashMap<>();
    private final Map<String, String> texts = new HashMap<>();
    private final Map<String, List<String>> lists = new HashMap<>();

    public TextDictionary(String templatePath) {
        this.parse(TemplateReader.read(templatePath));
    }

    private void parse(String content) {
        Matcher listMatcher = LIST_PATTERN.matcher(content);
        while (listMatcher.find()) {
            String key = listMatcher.group(1);
            String value = normalize(listMatcher.group(2).trim());
            lists.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        content = LIST_PATTERN.matcher(content).replaceAll("");
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

    public List<String> getList(String id) {
        return lists.getOrDefault(id, List.of());
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