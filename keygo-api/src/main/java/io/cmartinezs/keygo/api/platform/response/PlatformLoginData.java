package io.cmartinezs.keygo.api.platform.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de respuesta para el login de plataforma dentro del flujo PKCE.
 *
 * <p>Contiene el authorization code que el frontend intercambiará
 * por tokens en POST /platform/oauth2/token.
 *
 * <p>Cuando el login es bloqueado por {@code RESET_PASSWORD_REQUIRED} (HTTP 401), se retorna
 * únicamente {@code resetCodeId} con los demás campos nulos (omitidos por Jackson NON_NULL).
 *
 * @param message     mensaje descriptivo (null en 401 reset-required)
 * @param code        authorization code único (null en 401 reset-required)
 * @param redirectUri URI de redirección para el frontend (null en 401 reset-required)
 * @param resetCodeId UUID de la solicitud de reset (solo en 401 reset-required)
 * @author cmartinezs
 * @version 1.1
 */
public record PlatformLoginData(
    String message,
    String code,
    @JsonProperty("redirect_uri") String redirectUri,
    @JsonProperty("reset_code_id") String resetCodeId,
    @JsonProperty("notification_email") String notificationEmail) {

  /** Constructor de conveniencia para login exitoso (sin resetCodeId ni notificationEmail). */
  public PlatformLoginData(String message, String code, String redirectUri) {
    this(message, code, redirectUri, null, null);
  }
}
