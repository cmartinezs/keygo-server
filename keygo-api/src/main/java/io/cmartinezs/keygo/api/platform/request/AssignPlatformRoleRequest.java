package io.cmartinezs.keygo.api.platform.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for assigning a platform role to a user.
 * <p>DTO de solicitud para asignar un rol de plataforma a un usuario.
 *
 * @author cmartinezs
 * @version 1.0
 */
public record AssignPlatformRoleRequest(

    @NotBlank(message = "Role code is required")
    String roleCode
) {}
