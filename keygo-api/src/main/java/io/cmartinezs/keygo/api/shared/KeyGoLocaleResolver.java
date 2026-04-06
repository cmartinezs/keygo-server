package io.cmartinezs.keygo.api.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.servlet.LocaleResolver;

/**
 * Custom locale resolver for KeyGo that reads the {@code Accept-Language} HTTP header,
 * normalizes non-standard locale notations (underscore → hyphen, e.g. {@code en_US} → {@code en-US}),
 * and matches against the configured supported locales using {@link Locale#lookup}.
 *
 * <p>Motivation: the standard {@link org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver}
 * delegates parsing to the servlet container ({@code request.getLocales()}). Tomcat follows RFC 7231
 * strictly and interprets {@code en_US} (underscore) as an unknown language tag — resulting in a
 * non-matching locale that falls through to the default. This resolver bypasses the container and
 * parses the raw header directly via {@link Locale.LanguageRange#parse}, which is more lenient and
 * allows normalisation before matching.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>Parse + normalise the raw {@code Accept-Language} header value.</li>
 *   <li>Use {@link Locale#lookup} to find the best match from {@link #supportedLocales}.</li>
 *   <li>If no match (or header absent / malformed), return {@link #defaultLocale}.</li>
 * </ol>
 *
 * <p>This bean is used both by Spring MVC's {@code DispatcherServlet} (via the {@code localeResolver}
 * bean name) and by {@code LocaleContextFilter} for requests that never reach the DispatcherServlet
 * (e.g. 401 errors generated directly in security filters).
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
public class KeyGoLocaleResolver implements LocaleResolver {

  /** Default locale used when no supported locale can be matched. */
  private Locale defaultLocale = Locale.US;

  /**
   * Locales that this resolver accepts. The first match (per {@link Locale#lookup} priority rules)
   * wins. Must not be {@code null}; an empty list forces every request to {@link #defaultLocale}.
   */
  private List<Locale> supportedLocales = List.of(
      Locale.forLanguageTag("es"),
      Locale.forLanguageTag("es-CL"),
      Locale.forLanguageTag("en"),
      Locale.forLanguageTag("en-US")
  );

  /**
   * Resolves the best-matching locale for the given request.
   *
   * <p>Reads the raw {@code Accept-Language} header, normalises underscores to hyphens, then uses
   * {@link Locale#lookup} to find the first supported locale that satisfies the client's preference.
   * Falls back to {@link #defaultLocale} when the header is absent, blank, malformed, or when no
   * supported locale matches.
   *
   * @param request the incoming HTTP request
   * @return the resolved {@link Locale}, never {@code null}
   */
  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    String header = request.getHeader("Accept-Language");
    return resolveFromHeader(header);
  }

  /**
   * Locale-setting is not supported by this read-only resolver; always throws.
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
    throw new UnsupportedOperationException(
        "KeyGoLocaleResolver does not support explicit locale changes per request.");
  }

  /**
   * Resolves a {@link Locale} from a raw {@code Accept-Language} header string.
   *
   * <p>This method is package-visible for unit testing.
   *
   * @param header raw header value (may be {@code null} or blank)
   * @return the resolved locale, never {@code null}
   */
  Locale resolveFromHeader(String header) {
    if (header == null || header.isBlank()) {
      return defaultLocale;
    }
    // Normalise: en_US → en-US, es_CL → es-CL (underscore is not valid BCP 47)
    String normalised = header.replace('_', '-');
    try {
      List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(normalised);
      Locale matched = Locale.lookup(ranges, supportedLocales);
      return matched != null ? matched : defaultLocale;
    } catch (IllegalArgumentException e) {
      // Malformed Accept-Language header → fall back
      return defaultLocale;
    }
  }
}

