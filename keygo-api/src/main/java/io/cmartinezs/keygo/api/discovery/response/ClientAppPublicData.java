package io.cmartinezs.keygo.api.discovery.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Public-facing DTO for client application discovery — exposes only fields needed for
 * the self-registration selection UX.
 * <p>DTO público para descubrimiento de apps cliente — expone solo los campos necesarios
 * para la UX de selección en auto-registro.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class ClientAppPublicData {

  /** Internal UUID of the client application. */
  private final String id;

  /** OAuth2 client_id (globally unique). */
  private final String clientId;

  /** Human-readable application name. */
  private final String name;

  /** Optional description of the application. */
  private final String description;

  /** Client type: PUBLIC or CONFIDENTIAL. */
  private final String type;

  /** Registration policy controlling self-registration behavior. */
  private final String registrationPolicy;

  /** True if the application is currently active. */
  private final boolean isActive;
}
