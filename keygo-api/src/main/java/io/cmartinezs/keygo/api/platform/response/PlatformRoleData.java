package io.cmartinezs.keygo.api.platform.response;

import io.cmartinezs.keygo.app.membership.result.GetPlatformRolesCatalogResult;

/**
 * Response DTO representing a platform role available for assignment.
 *
 * @param id the platform role identifier
 * @param code the platform role code
 * @param name the display name of the role
 * @param description the optional role description
 */
public record PlatformRoleData(String id, String code, String name, String description) {

  public static PlatformRoleData from(GetPlatformRolesCatalogResult result) {
    return new PlatformRoleData(
        result.id().toString(),
        result.code(),
        result.name(),
        result.description());
  }
}
