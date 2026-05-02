package io.cmartinezs.keygo.api.membership.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for assigning a parent role to a child role.
 *
 * @param parentRoleCode code of the role that will become the parent
 */
public record AssignRoleParentRequest(
    @NotBlank String parentRoleCode
) {}
