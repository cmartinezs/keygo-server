package io.cmartinezs.keygo.api.clientapp.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Response DTO with client application data.
 * <p>DTO de respuesta con datos de la aplicación cliente.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class ClientAppData {

  /* The internal UUID of the client application */
  private String id;

  /* The OAuth2 client_id */
  private String clientId;

  /* Human-readable name */
  private String name;

  /* Optional description */
  private String description;

  /* CLIENT type: PUBLIC or CONFIDENTIAL */
  private String type;

  /* Registered redirect URIs */
  private List<String> redirectUris;

  /* Allowed OAuth2 grant types */
  private List<String> grants;

  /* Allowed OAuth2 scopes */
  private List<String> scopes;

  /* Current status: ACTIVE, SUSPENDED, PENDING */
  private String status;

  /* Creation timestamp */
  private OffsetDateTime createdAt;

  /* Last update timestamp */
  private OffsetDateTime updatedAt;
}

