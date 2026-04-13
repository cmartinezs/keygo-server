package io.cmartinezs.keygo.api.platform.response;

import io.cmartinezs.keygo.app.membership.result.PlatformRoleContractorResult;
import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO with contractor information associated to a scoped platform role assignment.
 */
@Getter
@Builder
public class PlatformRoleContractorData {

  private final String id;
  private final String displayName;
  private final String billingEmail;

  public static PlatformRoleContractorData from(PlatformRoleContractorResult result) {
    return PlatformRoleContractorData.builder()
        .id(result.id().toString())
        .displayName(result.displayName())
        .billingEmail(result.billingEmail())
        .build();
  }
}
