package io.cmartinezs.keygo.api.shared;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;

@RequiredArgsConstructor
public class MessageTranslator {
  private final MessageSource messageSource;

  public String getMessage(String code, Locale locale, Object... args) {
    return messageSource.getMessage(code, args, locale);
  }

  public String getMessageOrDefault(
      String code, String defaultMessage, Locale locale, Object... args) {
    return messageSource.getMessage(code, args, defaultMessage, locale);
  }
}
