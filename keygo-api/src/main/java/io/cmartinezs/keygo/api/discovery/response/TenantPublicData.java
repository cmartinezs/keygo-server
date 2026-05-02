package io.cmartinezs.keygo.api.discovery.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Public-facing DTO for tenant discovery — exposes only non-sensitive fields.
 * <p>DTO público para descubrimiento de tenants — expone solo campos no sensibles.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class TenantPublicData {

  /** Internal UUID of the tenant. */
  private final String id;

  /** Human-readable tenant name. */
  private final String name;

  /** URL-friendly unique identifier. */
  private final String slug;
}
