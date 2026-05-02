package io.cmartinezs.keygo.app.user.result;

import java.util.List;

/**
 * Resultado de la lista de sesiones del usuario autenticado.
 *
 * @param sessions lista de sesiones activas ordenada por último acceso DESC
 */
public record ListUserSessionsResult(List<SessionInfoResult> sessions) {}
