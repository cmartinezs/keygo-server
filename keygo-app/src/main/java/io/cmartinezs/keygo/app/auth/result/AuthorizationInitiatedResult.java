package io.cmartinezs.keygo.app.auth.result;

/**
 * Resultado de iniciar autorización: confirma que el cliente es válido y el flujo puede proceder.
 *
 * @param clientId client_id del cliente
 * @param clientName nombre de la app cliente (para mostrar al usuario)
 * @param redirectUri URI de redirección
 */
public record AuthorizationInitiatedResult(String clientId, String clientName, String redirectUri) {}

