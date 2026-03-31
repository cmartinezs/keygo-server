package io.cmartinezs.keygo.api.auth.response;

/**
 * Response: Login exitoso + authorization code emitido.
 *
 * <p>Retorna el código de autorización temporal que el cliente puede canjear por token en Fase 6.
 *
 * @param message mensaje de confirmación
 * @param code código de autorización temporal
 * @param redirectUri URI de redirección del cliente
 */
public record LoginData(
    @com.fasterxml.jackson.annotation.JsonProperty("message") String message,
    @com.fasterxml.jackson.annotation.JsonProperty("code") String code,
    @com.fasterxml.jackson.annotation.JsonProperty("redirect_uri") String redirectUri) {

}
