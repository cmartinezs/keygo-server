package io.cmartinezs.keygo.api.shared;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Resolves user locale from HTTP Accept-Language header.
 * Supports: es (Spanish), en (English), pt (Portuguese), fr (French).
 * Falls back to en-US if header is missing or unsupported.
 */
@Component
public class LocaleResolver {

  private static final Locale DEFAULT_LOCALE = Locale.US; // en-US
  private static final List<String> SUPPORTED_LANGUAGES = List.of("es", "en", "pt", "fr");

  /**
   * Resolves locale from request's Accept-Language header.
   *
   * @param request HTTP servlet request
   * @return Resolved locale, or DEFAULT_LOCALE (en-US) if header is missing/unsupported
   */
  public Locale resolveLocale(HttpServletRequest request) {
    String headerValue = request.getHeader("Accept-Language");
    Locale fromHeader = parseAcceptLanguage(headerValue);

    if (fromHeader != null && isSupported(fromHeader)) {
      return fromHeader;
    }

    return DEFAULT_LOCALE;
  }

  /**
   * Parses Accept-Language header to extract primary locale.
   * Example: "es-CL,es;q=0.9,en-US;q=0.8" → Locale("es", "CL")
   * Handles q-values: "fr;q=0.5" → Locale("fr")
   *
   * @param headerValue Accept-Language header value
   * @return Parsed locale, or null if parsing fails
   */
  private Locale parseAcceptLanguage(String headerValue) {
    if (headerValue == null || headerValue.isBlank()) {
      return null;
    }

    // Split by comma to get first preference (highest quality)
    String firstPreference = headerValue.split(",")[0].trim();

    // Remove q-value parameter (e.g., "fr;q=0.5" → "fr")
    firstPreference = firstPreference.split(";")[0].trim();

    // Split by hyphen to extract language and optional region
    String[] parts = firstPreference.split("-");

    if (parts.length >= 1) {
      String language = parts[0].trim().toLowerCase();
      String region = parts.length > 1 ? parts[1].trim().toUpperCase() : "";

      return region.isEmpty() ? new Locale(language) : new Locale(language, region);
    }

    return null;
  }

  /**
   * Checks if locale is in the list of supported languages.
   *
   * @param locale Locale to check
   * @return true if locale language is supported
   */
  private boolean isSupported(Locale locale) {
    return SUPPORTED_LANGUAGES.contains(locale.getLanguage());
  }
}
