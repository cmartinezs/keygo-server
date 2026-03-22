package io.cmartinezs.keygo.app.auth.result;

/**
 * Resultado de emitir un código de autorización.
 *
 * @param code valor del código (aleatorio)
 * @param redirectUri URI de redirección donde enviar el código
 */
public record AuthorizationCodeIssuedResult(String code, String redirectUri) {}

