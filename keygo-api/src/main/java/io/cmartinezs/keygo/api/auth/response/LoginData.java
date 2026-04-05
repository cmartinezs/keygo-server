package io.cmartinezs.keygo.api.auth.response;

/**
 * Response: Login exitoso + authorization code emitido.
 *
 * <p>Retorna el código de autorización temporal que el cliente puede canjear por token.
 *
 * <p>Cuando el login es bloqueado por {@code RESET_PASSWORD_REQUIRED} (HTTP 401), se retorna
 * únicamente {@code resetCodeId} con los demás campos nulos (omitidos por Jackson NON_NULL).
 * El frontend debe redirigir al formulario de reset incluyendo el {@code resetCodeId} como
 * query param para identificar la solicitud.
 *
 * @param message     mensaje de confirmación (null en 401 reset-required)
 * @param code        código de autorización temporal (null en 401 reset-required)
 * @param redirectUri URI de redirección del cliente (null en 401 reset-required)
 * @param resetCodeId UUID de la solicitud de reset en {@code password_reset_codes}
 *                    (solo presente en 401 reset-required; null en login exitoso)
 */
public record LoginData(
    @com.fasterxml.jackson.annotation.JsonProperty("message") String message,
    @com.fasterxml.jackson.annotation.JsonProperty("code") String code,
    @com.fasterxml.jackson.annotation.JsonProperty("redirect_uri") String redirectUri,
    @com.fasterxml.jackson.annotation.JsonProperty("reset_code_id") String resetCodeId) {

  /** Constructor de conveniencia para login exitoso (sin resetCodeId). */
  public LoginData(String message, String code, String redirectUri) {
    this(message, code, redirectUri, null);
  }
}
