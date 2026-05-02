package io.cmartinezs.keygo.domain.shared.util;

import java.util.Arrays;

/**
 * Utilidad para ofuscar direcciones de correo electrónico en respuestas de la API.
 *
 * <p>Reglas de ofuscación:
 * <ul>
 *   <li><b>Parte local:</b> primera y última letra visibles, mínimo 8 asteriscos entre ellas.</li>
 *   <li><b>Dominio:</b> primera letra visible, mínimo 4 asteriscos, extensión TLD visible.</li>
 *   <li><b>TLD compuesto:</b> si el último segmento tiene ≤2 caracteres (código país), se incluye
 *       también el penúltimo segmento (e.g. {@code .com.br}, {@code .co.uk}).</li>
 * </ul>
 *
 * <p>Ejemplos:
 * <pre>
 *   admin@keygo.local      → a********n@k****.local
 *   test@example.com.br    → t********t@e******.com.br
 *   ab@gmail.com           → a********b@g****.com
 *   u@d.com                → u********@d****.com
 * </pre>
 *
 * @author cmartinezs
 * @version 1.0
 */
public final class EmailMasker {

  private EmailMasker() {}

  /**
   * Retorna la versión ofuscada del email, o el valor original si es nulo o no contiene {@code @}.
   *
   * @param email dirección de correo a ofuscar
   * @return email ofuscado
   */
  public static String mask(String email) {
    if (email == null || !email.contains("@")) {
      return email;
    }

    int atIdx = email.indexOf('@');
    String local = email.substring(0, atIdx);
    String domain = email.substring(atIdx + 1);

    return maskLocal(local) + "@" + maskDomain(domain);
  }

  private static final int MIN_LOCAL_STARS = 8;
  private static final int MIN_DOMAIN_STARS = 4;

  private static String maskLocal(String local) {
    if (local.length() <= 1) {
      return local + "*".repeat(MIN_LOCAL_STARS);
    }
    int stars = Math.max(MIN_LOCAL_STARS, local.length() - 2);
    return local.charAt(0) + "*".repeat(stars) + local.charAt(local.length() - 1);
  }

  private static String maskDomain(String domain) {
    String[] parts = domain.split("\\.");
    if (parts.length < 2) {
      return domain;
    }

    String tld;
    String domainName;
    String lastPart = parts[parts.length - 1];

    if (lastPart.length() <= 2 && parts.length >= 3) {
      tld = "." + parts[parts.length - 2] + "." + lastPart;
      domainName = String.join(".", Arrays.copyOf(parts, parts.length - 2));
    } else {
      tld = "." + lastPart;
      domainName = String.join(".", Arrays.copyOf(parts, parts.length - 1));
    }

    if (domainName.length() <= 1) {
      return domainName + "*".repeat(MIN_DOMAIN_STARS) + tld;
    }
    int stars = Math.max(MIN_DOMAIN_STARS, domainName.length() - 1);
    return domainName.charAt(0) + "*".repeat(stars) + tld;
  }
}
