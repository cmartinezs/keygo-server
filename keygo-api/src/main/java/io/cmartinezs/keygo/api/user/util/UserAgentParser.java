package io.cmartinezs.keygo.api.user.util;

/**
 * Utilidad de presentación: parsea un User-Agent raw para extraer información
 * legible para la UI (browser, OS, deviceType).
 *
 * <p>Implementación heurística ligera — no requiere librerías externas.
 * Suficiente para los casos habituales: Chrome/Firefox/Safari en Desktop/Mobile.
 */
public final class UserAgentParser {

  private UserAgentParser() {}

  /**
   * Extrae el nombre del navegador a partir del User-Agent raw.
   *
   * @param userAgent User-Agent string (puede ser null)
   * @return nombre del navegador o "Unknown"
   */
  public static String extractBrowser(String userAgent) {
    if (userAgent == null || userAgent.isBlank()) return "Unknown";
    if (userAgent.contains("Edg/") || userAgent.contains("Edge/")) return "Edge";
    if (userAgent.contains("OPR/") || userAgent.contains("Opera/")) return "Opera";
    if (userAgent.contains("Chrome/") && !userAgent.contains("Chromium/")) return "Chrome";
    if (userAgent.contains("Firefox/")) return "Firefox";
    if (userAgent.contains("Safari/") && !userAgent.contains("Chrome/")) return "Safari";
    if (userAgent.contains("MSIE") || userAgent.contains("Trident/")) return "Internet Explorer";
    return "Other";
  }

  /**
   * Extrae el sistema operativo a partir del User-Agent raw.
   *
   * @param userAgent User-Agent string (puede ser null)
   * @return nombre del SO o "Unknown"
   */
  public static String extractOs(String userAgent) {
    if (userAgent == null || userAgent.isBlank()) return "Unknown";
    if (userAgent.contains("Windows NT")) return "Windows";
    if (userAgent.contains("Mac OS X") || userAgent.contains("Macintosh")) return "macOS";
    if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
    if (userAgent.contains("Android")) return "Android";
    if (userAgent.contains("Linux")) return "Linux";
    return "Other";
  }

  /**
   * Infiere el tipo de dispositivo a partir del User-Agent raw.
   *
   * @param userAgent User-Agent string (puede ser null)
   * @return "mobile", "tablet" o "desktop"
   */
  public static String extractDeviceType(String userAgent) {
    if (userAgent == null || userAgent.isBlank()) return "desktop";
    String ua = userAgent.toLowerCase();
    if (ua.contains("ipad") || ua.contains("tablet")) return "tablet";
    if (ua.contains("mobile") || ua.contains("iphone") || ua.contains("android")) return "mobile";
    return "desktop";
  }
}
