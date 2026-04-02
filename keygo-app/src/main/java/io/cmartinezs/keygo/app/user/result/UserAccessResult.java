package io.cmartinezs.keygo.app.user.result;

import io.cmartinezs.keygo.domain.membership.model.UserAccessEntry;

import java.util.List;

/**
 * Resultado de la consulta de acceso self-service del usuario.
 *
 * @param entries lista de entradas de acceso (una por membresía)
 */
public record UserAccessResult(List<UserAccessEntry> entries) {}
