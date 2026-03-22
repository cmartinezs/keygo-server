package io.cmartinezs.keygo.app.auth.result;

/**
 * Resultado de canjear un código por token.
 *
 * <p>En esta fase solo retorna una confirmación de éxito. Tokens se emitirán en Fase 6.
 *
 * @param authorizationCodeId ID del código canjeado (para auditoría)
 */
public record ExchangeAuthorizationCodeResult(String authorizationCodeId) {}

