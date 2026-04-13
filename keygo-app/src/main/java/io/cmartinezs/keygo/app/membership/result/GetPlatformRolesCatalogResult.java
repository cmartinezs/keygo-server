package io.cmartinezs.keygo.app.membership.result;

import java.util.UUID;

/**
 * Read model for a platform role available in the platform catalog.
 *
 * @param id the platform role identifier
 * @param code the platform role code
 * @param name the display name of the role
 * @param description the optional role description
 */
public record GetPlatformRolesCatalogResult(UUID id, String code, String name, String description) {}
