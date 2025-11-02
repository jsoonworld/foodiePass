package foodiepass.server.menu.infra.scraper.gemini;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GeminiResponseParser {

    private static final Pattern MARKDOWN_JSON_PATTERN =
            Pattern.compile("(?s)```(?:json)?\\s*(.*?)\\s*```");

    public String parse(final String rawResponse) {
        final String decoded = StringEscapeUtils.unescapeJava(rawResponse);
        return extractJsonFromMarkdown(decoded);
    }

    private String extractJsonFromMarkdown(final String text) {
        Matcher matcher = MARKDOWN_JSON_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return text.trim();
    }
}
