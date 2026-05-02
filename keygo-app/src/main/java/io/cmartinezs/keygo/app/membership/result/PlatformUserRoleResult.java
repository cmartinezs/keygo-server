package io.cmartinezs.keygo.app.membership.result;

import java.time.Instant;
import java.util.UUID;

/**
 * Read model for a platform role assigned to a platform user.
 *
 * @param assignmentId the platform user role assignment identifier
 * @param roleId the platform role identifier
 * @param roleCode the platform role code
 * @param roleName the display name of the role
 * @param description the optional role description
 * @param scopeType the assignment scope type
 * @param contractorId optional contractor identifier associated to the assignment
 * @param tenantId optional tenant identifier associated to the assignment
 * @param contractor optional contractor summary when the assignment is contractor-scoped
 * @param assignedAt when the role was assigned
 */
public record PlatformUserRoleResult(
    UUID assignmentId,
    UUID roleId,
    String roleCode,
    String roleName,
    String description,
    String scopeType,
    UUID contractorId,
    UUID tenantId,
    PlatformRoleContractorResult contractor,
    Instant assignedAt) {}
