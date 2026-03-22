package io.cmartinezs.keygo.api.membership.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating a membership.
 * <p>DTO de request para crear una membresía.
 * @author cmartinezs
 * @version 1.0
 */
public record CreateMembershipRequest(
    @NotNull(message = "userId is required")
    UUID userId,
    @NotNull(message = "clientAppId is required")
    UUID clientAppId,
    @NotEmpty(message = "At least one role code is required")
    Set<String> roleCodes
) {}

