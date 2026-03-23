package io.cmartinezs.keygo.app.auth.command;

/**
 * Command: emitir un access_token para el flujo OAuth2 client_credentials.
 *
 * <p>No contiene información de usuario final — el sujeto del token es la propia app cliente.
 *
 * @param tenantSlug      slug del tenant al que pertenece la app
 * @param clientId        identificador público de la app (client_id)
 * @param rawClientSecret secret en texto plano a verificar contra el hash almacenado
 * @param scope           scopes solicitados (puede ser null; se usan los de la política si es null)
 */
public record IssueClientCredentialsTokenCommand(
    String tenantSlug,
    String clientId,
    String rawClientSecret,
    String scope) {}

