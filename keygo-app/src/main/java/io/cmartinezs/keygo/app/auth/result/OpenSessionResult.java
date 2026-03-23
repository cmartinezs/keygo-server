package io.cmartinezs.keygo.app.auth.result;

/**
 * Resultado de abrir una nueva sesión.
 *
 * @param sessionId ID de la sesión creada (UUID como String)
 */
public record OpenSessionResult(String sessionId) {}

