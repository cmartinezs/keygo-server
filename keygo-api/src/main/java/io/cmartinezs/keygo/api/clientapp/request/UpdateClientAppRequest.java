package io.cmartinezs.keygo.api.clientapp.request;

import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * Request DTO for updating an existing client application.
 * <p>DTO de solicitud para actualizar una aplicación cliente existente.
 * @author cmartinezs
 * @version 1.0
 */
public record UpdateClientAppRequest(
    @NotBlank String name,
    String description,
    Set<String> redirectUris,
    @NotEmpty Set<AllowedGrant> grants,
    Set<String> scopes
) {}

