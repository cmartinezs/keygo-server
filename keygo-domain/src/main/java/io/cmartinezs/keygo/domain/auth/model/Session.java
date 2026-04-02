package io.cmartinezs.keygo.domain.auth.model;

import io.cmartinezs.keygo.domain.auth.exception.SessionInvalidStateException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.time.Instant;
import lombok.Getter;

/**
 * Agregado: Sesión de usuario OAuth2.
 *
 * <p>Representa una sesión asociada al ciclo de vida de un refresh token.
 * Una sesión agrupa todos los refresh tokens emitidos para un usuario en una app concreta.
 *
 * <p>Invariantes:
 * <ul>
 *   <li>Una sesión solo puede terminar si está ACTIVE
 *   <li>Una sesión expirada no puede reactivarse
 * </ul>
 */
@Getter
public class Session {
  private final SessionId id;
  private final TenantId tenantId;
  private final ClientAppId clientAppId;
  private final UserId userId;
  private SessionStatus status;
  private final Instant expiresAt;
  private Instant lastAccessedAt;
  private final String userAgent;
  private final String ipAddress;
  private final Instant createdAt;

  private Session(
      SessionId id,
      TenantId tenantId,
      ClientAppId clientAppId,
      UserId userId,
      SessionStatus status,
      Instant expiresAt,
      Instant lastAccessedAt,
      String userAgent,
      String ipAddress,
      Instant createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.clientAppId = clientAppId;
    this.userId = userId;
    this.status = status;
    this.expiresAt = expiresAt;
    this.lastAccessedAt = lastAccessedAt;
    this.userAgent = userAgent;
    this.ipAddress = ipAddress;
    this.createdAt = createdAt;
  }

  /**
   * Factory method: crea una sesión nueva en estado ACTIVE.
   *
   * @param tenantId    tenant propietario
   * @param clientAppId app cliente
   * @param userId      usuario
   * @param expiresAt   expiración de la sesión
   * @param now         instante de creación
   * @param userAgent   user-agent del cliente (nullable)
   * @param ipAddress   dirección IP del cliente (nullable)
   * @return nueva sesión en estado ACTIVE
   */
  public static Session open(
      TenantId tenantId,
      ClientAppId clientAppId,
      UserId userId,
      Instant expiresAt,
      Instant now,
      String userAgent,
      String ipAddress) {
    if (tenantId == null) throw new IllegalArgumentException("TenantId cannot be null");
    if (clientAppId == null) throw new IllegalArgumentException("ClientAppId cannot be null");
    if (userId == null) throw new IllegalArgumentException("UserId cannot be null");
    if (expiresAt == null) throw new IllegalArgumentException("ExpiresAt cannot be null");
    if (now == null) throw new IllegalArgumentException("Now cannot be null");

    return new Session(
        SessionId.generate(),
        tenantId,
        clientAppId,
        userId,
        SessionStatus.ACTIVE,
        expiresAt,
        now,
        userAgent,
        ipAddress,
        now);
  }

  /**
   * Reconstruye una sesión desde persistencia.
   */
  public static Session reconstitute(
      SessionId id,
      TenantId tenantId,
      ClientAppId clientAppId,
      UserId userId,
      SessionStatus status,
      Instant expiresAt,
      Instant lastAccessedAt,
      String userAgent,
      String ipAddress,
      Instant createdAt) {
    return new Session(id, tenantId, clientAppId, userId, status, expiresAt, lastAccessedAt, userAgent, ipAddress, createdAt);
  }

  /** @return true si la sesión está activa */
  public boolean isActive() {
    return status == SessionStatus.ACTIVE;
  }

  /**
   * Termina la sesión. Solo aplicable si está ACTIVE.
   *
   * @throws IllegalStateException si la sesión no está ACTIVE
   */
  public void terminate() {
    if (status != SessionStatus.ACTIVE) {
      throw new SessionInvalidStateException(id.value(), status, "terminate");
    }
    this.status = SessionStatus.TERMINATED;
  }

  /**
   * Actualiza el timestamp de último acceso.
   *
   * @param accessedAt nuevo instante de acceso
   */
  public void updateLastAccessed(Instant accessedAt) {
    if (accessedAt == null) throw new IllegalArgumentException("AccessedAt cannot be null");
    this.lastAccessedAt = accessedAt;
  }
}

