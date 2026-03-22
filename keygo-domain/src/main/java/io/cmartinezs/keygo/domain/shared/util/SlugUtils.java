package io.cmartinezs.keygo.domain.shared.util;

import java.text.Normalizer;
import lombok.experimental.UtilityClass;

/**
 * Utility class for generating URL-friendly slugs from arbitrary strings.
 *
 * <p>Clase utilitaria para generar slugs amigables para URLs a partir de cadenas arbitrarias.
 *
 * <p>Algorithm:
 *
 * <ol>
 *   <li>Trim whitespace.
 *   <li>NFD-normalize to decompose accented characters.
 *   <li>Strip non-ASCII characters (removes diacritics after decomposition).
 *   <li>Lowercase.
 *   <li>Replace every run of non-alphanumeric characters with a single hyphen.
 *   <li>Strip leading and trailing hyphens.
 *   <li>Truncate to {@code maxLength} characters and strip any trailing hyphen introduced by
 *       truncation.
 * </ol>
 *
 * @author cmartinezs
 * @version 1.0
 */
@UtilityClass
public final class SlugUtils {

  /* Default maximum slug length aligned with TenantSlug.MAX_LENGTH */
  private static final int DEFAULT_MAX_LENGTH = 100;

  /**
   * Convert {@code input} to a URL-friendly slug using the default maximum length of 100.
   *
   * @param input the raw string (e.g. a tenant name)
   * @return the generated slug
   * @throws IllegalArgumentException if {@code input} is {@code null}, blank, or produces an empty
   *     slug after normalization
   */
  public static String toSlug(String input) {
    return toSlug(input, DEFAULT_MAX_LENGTH);
  }

  /**
   * Convert {@code input} to a URL-friendly slug, truncating to {@code maxLength}.
   *
   * @param input the raw string
   * @param maxLength the maximum number of characters allowed in the result
   * @return the generated slug
   * @throws IllegalArgumentException if {@code input} is {@code null}, blank, or produces an empty
   *     slug after normalization
   */
  public static String toSlug(String input, int maxLength) {
    if (input == null || input.isBlank()) {
      throw new IllegalArgumentException("Cannot generate a slug from a null or blank input");
    }

    // 1. Normalize: decompose accented characters (e.g. é → e + ´)
    String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);

    // 2. Remove non-ASCII characters (strips diacritics produced by NFD)
    String trimmed = getTrimmed(input, normalized);

    // 6. Truncate and remove any trailing hyphen introduced by the cut
    if (trimmed.length() > maxLength) {
      trimmed = trimmed.substring(0, maxLength).replaceAll("-+$", "");
    }

    return trimmed;
  }

  private static String getTrimmed(String input, String normalized) {
    String ascii = normalized.replaceAll("[^\\p{ASCII}]", "");

    // 3. Lowercase
    String lower = ascii.toLowerCase();

    // 4. Replace every run of non-alphanumeric characters with a single hyphen
    String hyphenated = lower.replaceAll("[^a-z0-9]+", "-");

    // 5. Strip leading and trailing hyphens
    String trimmed = hyphenated.replaceAll("(^-+)|(-+$)", "");

    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException(
          "Input '" + input + "' produces an empty slug after normalization");
    }
    return trimmed;
  }
}
