package io.cmartinezs.keygo.api.platform.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for assigning one or more platform roles to a user.
 * <p>DTO de solicitud para asignar uno o más roles de plataforma a un usuario.
 *
 * @author cmartinezs
 * @version 1.0
 */
public record AssignPlatformRoleRequest(

    @NotEmpty(message = "At least one role code is required")
    List<String> roleCodes
) {}
