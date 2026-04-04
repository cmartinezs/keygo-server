package io.cmartinezs.keygo.api.shared;

import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class I18nConfig {
  @Bean
  public LocaleResolver localeResolver() {
    var resolver = new AcceptHeaderLocaleResolver();

    resolver.setDefaultLocale(Locale.forLanguageTag("es-CL"));

    resolver.setSupportedLocales(
        List.of(
            Locale.forLanguageTag("es"),
            Locale.forLanguageTag("es-CL"),
            Locale.forLanguageTag("en"),
            Locale.forLanguageTag("en-US")));

    return resolver;
  }

  @Bean
  public MessageTranslator getMessageTranslator(MessageSource messageSource) {
    return new MessageTranslator(messageSource);
  }
}
