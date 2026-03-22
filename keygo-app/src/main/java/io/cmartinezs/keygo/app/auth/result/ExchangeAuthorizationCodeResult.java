package io.cmartinezs.keygo.app.auth.result;

/**
 * Resultado de canjear un código por token.
 *
 * <p>Incluye la información necesaria para que la capa de presentación invoque la emisión de tokens
 * JWT firmados (Fase 6).
 *
 * @param authorizationCodeId ID del código canjeado (para auditoría)
 * @param userId              UUID del usuario autenticado (como String)
 * @param clientId            client_id de la aplicación cliente
 * @param scope               scopes otorgados, separados por espacio
 */
public record ExchangeAuthorizationCodeResult(
    String authorizationCodeId,
    String userId,
    String clientId,
    String scope) {}

