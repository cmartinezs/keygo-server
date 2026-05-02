package io.cmartinezs.keygo.app.membership.result;

import java.util.UUID;

/**
 * Read model for contractor information associated to a scoped platform role assignment.
 *
 * @param id contractor identifier
 * @param displayName contractor display name
 * @param billingEmail contractor billing email
 */
public record PlatformRoleContractorResult(UUID id, String displayName, String billingEmail) {}
