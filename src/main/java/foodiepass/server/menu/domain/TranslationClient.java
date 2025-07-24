package foodiepass.server.menu.domain;

import foodiepass.server.language.domain.Language;
import lombok.NonNull;

public interface TranslationClient {
  String translate(@NonNull final Language from, @NonNull final Language to, @NonNull final String content);
}
