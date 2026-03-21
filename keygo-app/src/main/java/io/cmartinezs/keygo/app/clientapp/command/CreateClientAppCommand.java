package io.cmartinezs.keygo.app.clientapp.command;

import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;

import java.util.Set;

/**
 * Command to create a new client application.
 * <p>Comando para crear una nueva aplicación cliente.
 * @author cmartinezs
 * @version 1.0
 */
public record CreateClientAppCommand(
    String tenantSlug,
    String name,
    String description,
    ClientType type,
    Set<String> redirectUris,
    Set<AllowedGrant> grants,
    Set<String> scopes
) {}

