package io.cmartinezs.keygo.domain.clientapp.model;

import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppAlreadySuspendedException;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppSecretRotationException;
import io.cmartinezs.keygo.domain.clientapp.exception.InvalidRedirectUriException;
import io.cmartinezs.keygo.domain.clientapp.exception.UnsupportedGrantTypeException;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/**
 * ClientApp domain aggregate — represents an OAuth2 client application registered under a tenant.
 * <p>Agregado de dominio ClientApp — representa una aplicación cliente OAuth2
 * registrada bajo un tenant.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class ClientApp {

  private final ClientAppId id;
  private final TenantId tenantId;
  private final ClientId clientId;
  private String name;
  private String description;
  private final ClientType type;
  private String hashedSecret;
  private Set<RedirectUri> redirectUris;
  private AccessPolicy accessPolicy;
  private ClientAppStatus status;

  @Builder
  private ClientApp(
      ClientAppId id,
      TenantId tenantId,
      ClientId clientId,
      String name,
      String description,
      ClientType type,
      String hashedSecret,
      Set<RedirectUri> redirectUris,
      AccessPolicy accessPolicy,
      ClientAppStatus status) {
    if (tenantId == null) throw new IllegalArgumentException("ClientApp tenantId cannot be null");
    if (clientId == null) throw new IllegalArgumentException("ClientApp clientId cannot be null");
    if (name == null || name.isBlank()) throw new IllegalArgumentException("ClientApp name cannot be null or blank");
    if (type == null) throw new IllegalArgumentException("ClientApp type cannot be null");
    if (status == null) throw new IllegalArgumentException("ClientApp status cannot be null");
    if (accessPolicy == null) throw new IllegalArgumentException("ClientApp accessPolicy cannot be null");
    if (ClientType.CONFIDENTIAL.equals(type) && (hashedSecret == null || hashedSecret.isBlank())) {
      throw new IllegalArgumentException("CONFIDENTIAL client apps must have a hashed secret");
    }

    this.id = id;
    this.tenantId = tenantId;
    this.clientId = clientId;
    this.name = name;
    this.description = description;
    this.type = type;
    this.hashedSecret = hashedSecret;
    this.redirectUris = redirectUris == null ? Set.of() : Set.copyOf(redirectUris);
    this.accessPolicy = accessPolicy;
    this.status = status;
  }

  // ─── Domain behaviour ─────────────────────────────────────────────────────

  /**
   * Returns true if the client application is active.
   * <p>Retorna true si la aplicación cliente está activa.
   */
  public boolean isActive() {
    return ClientAppStatus.ACTIVE.equals(this.status);
  }

  /**
   * Returns true if the client application is suspended.
   * <p>Retorna true si la aplicación cliente está suspendida.
   */
  public boolean isSuspended() {
    return ClientAppStatus.SUSPENDED.equals(this.status);
  }

  /**
   * Suspend this client application. Throws if already suspended.
   * <p>Suspende esta aplicación cliente. Lanza excepción si ya estaba suspendida.
   * @throws IllegalStateException if the client app is already suspended
   */
  public void suspend() {
    if (ClientAppStatus.SUSPENDED.equals(this.status)) {
      throw new ClientAppAlreadySuspendedException(clientId.value());
    }
    this.status = ClientAppStatus.SUSPENDED;
  }

  /**
   * Reactivate a previously suspended client application.
   * <p>Reactiva una aplicación cliente previamente suspendida.
   */
  public void activate() {
    this.status = ClientAppStatus.ACTIVE;
  }

  /**
   * Update the client app name and description.
   * <p>Actualiza el nombre y descripción de la aplicación cliente.
   * @param name new name
   * @param description new description (nullable)
   */
  public void updateInfo(String name, String description) {
    if (name == null || name.isBlank()) throw new IllegalArgumentException("ClientApp name cannot be null or blank");
    this.name = name;
    this.description = description;
  }

  /**
   * Update the redirect URIs of this client application.
   * <p>Actualiza los URIs de redirección de esta aplicación cliente.
   * @param redirectUris new set of redirect URIs
   */
  public void updateRedirectUris(Set<RedirectUri> redirectUris) {
    this.redirectUris = redirectUris == null ? Set.of() : Set.copyOf(redirectUris);
  }

  /**
   * Update the access policy of this client application.
   * <p>Actualiza la política de acceso de esta aplicación cliente.
   * @param accessPolicy the new access policy
   */
  public void updateAccessPolicy(AccessPolicy accessPolicy) {
    if (accessPolicy == null) throw new IllegalArgumentException("AccessPolicy cannot be null");
    this.accessPolicy = accessPolicy;
  }

  /**
   * Rotate the hashed secret for this client application.
   * <p>Rota el secret hasheado de esta aplicación cliente.
   * @param newHashedSecret the new hashed secret
   * @throws IllegalStateException if this is a PUBLIC client app
   */
  public void rotateSecret(String newHashedSecret) {
    if (ClientType.PUBLIC.equals(this.type)) {
      throw new ClientAppSecretRotationException(clientId.value());
    }
    if (newHashedSecret == null || newHashedSecret.isBlank()) {
      throw new IllegalArgumentException("New hashed secret cannot be null or blank");
    }
    this.hashedSecret = newHashedSecret;
  }

  /**
   * Validate that the given redirect URI is registered for this client app.
   * <p>Valida que el URI de redirección dado esté registrado en esta aplicación cliente.
   * @param uri the URI string to validate
   * @throws InvalidRedirectUriException if the URI is not registered
   */
  public void validateRedirectUri(String uri) {
    boolean found = redirectUris.stream().anyMatch(r -> r.value().equals(uri));
    if (!found) {
      throw new InvalidRedirectUriException(uri);
    }
  }

  /**
   * Validate that the given grant is allowed by this client app's access policy.
   * <p>Valida que el grant dado esté permitido por la política de acceso de esta aplicación.
   * @param grant the grant to validate
   * @throws UnsupportedGrantTypeException if the grant is not allowed
   */
  public void validateGrant(AllowedGrant grant) {
    if (!accessPolicy.allowsGrant(grant)) {
      throw new UnsupportedGrantTypeException(grant.name());
    }
  }

  @Override
  public String toString() {
    return "ClientApp{id=" + id + ", clientId=" + clientId + ", type=" + type + ", status=" + status + "}";
  }
}

