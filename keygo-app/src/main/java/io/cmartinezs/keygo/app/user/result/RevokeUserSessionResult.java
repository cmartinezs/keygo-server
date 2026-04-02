package io.cmartinezs.keygo.app.user.result;

import java.util.UUID;

/**
 * Resultado de la revocación de una sesión propia del usuario.
 *
 * @param sessionId    UUID de la sesión afectada
 * @param alreadyClosed true si la sesión ya estaba cerrada (idempotente — no es un error)
 */
public record RevokeUserSessionResult(UUID sessionId, boolean alreadyClosed) {}
