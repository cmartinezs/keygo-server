package io.cmartinezs.keygo.api.clientapp.request;

import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * Request DTO for creating a new client application.
 * <p>DTO de solicitud para crear una nueva aplicación cliente.
 * @author cmartinezs
 * @version 1.0
 */
public record CreateClientAppRequest(
    @NotBlank String name,
    String description,
    @NotNull ClientType type,
    Set<String> redirectUris,
    @NotEmpty Set<AllowedGrant> grants,
    Set<String> scopes
) {}

