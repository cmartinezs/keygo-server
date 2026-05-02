package io.cmartinezs.keygo.app.membership.command;

import java.util.UUID;

/**
 * Command for assigning a parent role to a child role within a client app.
 *
 * @param tenantSlug     slug of the owning tenant
 * @param clientAppId    UUID of the client app that owns both roles
 * @param childRoleCode  code of the role that will receive the parent
 * @param parentRoleCode code of the role that will become the parent
 */
public record AssignRoleParentCommand(
    String tenantSlug,
    UUID clientAppId,
    String childRoleCode,
    String parentRoleCode
) {}
