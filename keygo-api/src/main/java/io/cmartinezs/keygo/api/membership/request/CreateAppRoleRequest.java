package io.cmartinezs.keygo.api.membership.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating an app role.
 * <p>DTO de request para crear un rol de app.
 * @author cmartinezs
 * @version 1.0
 */
public record CreateAppRoleRequest(
    @NotBlank(message = "code is required")
    String code,
    String displayName,
    String description
) {}

