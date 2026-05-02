package io.cmartinezs.keygo.app.clientapp.command;

import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;

import java.util.Set;

/**
 * Command to update an existing client application.
 * <p>Comando para actualizar una aplicación cliente existente.
 * @author cmartinezs
 * @version 1.0
 */
public record UpdateClientAppCommand(
    String tenantSlug,
    String clientId,
    String name,
    String description,
    Set<String> redirectUris,
    Set<AllowedGrant> grants,
    Set<String> scopes
) {}

