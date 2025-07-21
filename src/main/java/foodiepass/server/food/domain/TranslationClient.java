package foodiepass.server.food.domain;

import foodiepass.server.language.domain.Language;

public interface TranslationClient {
  String translate(final Language from, final Language to, final String content);
}
