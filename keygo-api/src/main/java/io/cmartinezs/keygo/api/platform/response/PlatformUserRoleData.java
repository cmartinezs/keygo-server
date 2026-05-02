package io.cmartinezs.keygo.api.platform.response;

import io.cmartinezs.keygo.app.membership.result.PlatformUserRoleResult;
import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO representing a platform role assigned to a platform user.
 */
@Getter
@Builder
public class PlatformUserRoleData {

  private final String assignmentId;
  private final String roleId;
  private final String roleCode;
  private final String roleName;
  private final String description;
  private final String scopeType;
  private final String contractorId;
  private final String tenantId;
  private final PlatformRoleContractorData contractor;
  private final String assignedAt;

  public static PlatformUserRoleData from(PlatformUserRoleResult result) {
    return PlatformUserRoleData.builder()
        .assignmentId(result.assignmentId().toString())
        .roleId(result.roleId().toString())
        .roleCode(result.roleCode())
        .roleName(result.roleName())
        .description(result.description())
        .scopeType(result.scopeType())
        .contractorId(result.contractorId() != null ? result.contractorId().toString() : null)
        .tenantId(result.tenantId() != null ? result.tenantId().toString() : null)
        .contractor(
            result.contractor() != null ? PlatformRoleContractorData.from(result.contractor()) : null)
        .assignedAt(result.assignedAt().toString())
        .build();
  }
}
