package io.cmartinezs.keygo.api.shared;

import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * i18n configuration for the KeyGo API layer.
 *
 * <p>Registers a {@link KeyGoLocaleResolver} as the Spring MVC {@code localeResolver} bean.
 * The resolver normalises non-standard locale notations (e.g. {@code en_US} with underscore)
 * and falls back to {@code en-US} when the {@code Accept-Language} header is absent or
 * contains an unsupported locale.
 *
 * <p>Supported locales: {@code es}, {@code es-CL}, {@code en}, {@code en-US}.
 * Default (fallback) locale: {@code en-US} (per design doc I18N_STRATEGY.md §Restricciones).
 */
@Configuration
public class I18nConfig {

  /** Locales accepted by the API. Order determines lookup priority via {@link Locale#lookup}. */
  static final List<Locale> SUPPORTED_LOCALES = List.of(
      Locale.forLanguageTag("es"),
      Locale.forLanguageTag("es-CL"),
      Locale.forLanguageTag("en"),
      Locale.forLanguageTag("en-US"));

  /**
   * Spring MVC locale resolver bean.
   *
   * <p>Called by {@code DispatcherServlet} for every MVC request to populate
   * {@code LocaleContextHolder}. Also injected into {@code LocaleContextFilter} so that
   * requests that never reach the DispatcherServlet (e.g. 401 errors from security filters)
   * receive the same locale resolution logic.
   *
   * <p>Returns {@link KeyGoLocaleResolver} (concrete type) so it can be injected by type
   * in {@code ApplicationConfig} for the filter registration.
   */
  @Bean
  public KeyGoLocaleResolver localeResolver() {
    var resolver = new KeyGoLocaleResolver();
    resolver.setDefaultLocale(Locale.US);          // en-US fallback per I18N_STRATEGY.md
    resolver.setSupportedLocales(SUPPORTED_LOCALES);
    return resolver;
  }

  @Bean
  public MessageTranslator getMessageTranslator(MessageSource messageSource) {
    return new MessageTranslator(messageSource);
  }
}
