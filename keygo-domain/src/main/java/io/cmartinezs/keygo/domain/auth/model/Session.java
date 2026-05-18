package io.cmartinezs.keygo.domain.auth.model;

import io.cmartinezs.keygo.domain.auth.exception.SessionInvalidStateException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/**
 * Agregado: Sesión de usuario OAuth2.
 *
 * <p>Representa una sesión asociada al ciclo de vida de un refresh token.
 * Una sesión agrupa todos los refresh tokens emitidos para un usuario en una app concreta,
 * o una sesión de plataforma (sin app) para usuarios de KeyGo.
 *
 * <p>Modelo restructurado (RFC restructure-multitenant):
 * <ul>
 *   <li>{@code platformUserId} — identidad global (FK platform_users). Nullable para MVP.
 *   <li>{@code clientAppId} — app cliente. NULL = sesión de plataforma (KeyGo UI).
 * </ul>
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
  /** UUID del platform_user. Nullable para MVP (tenant-only users sin plataforma). */
  private final UUID platformUserId;
  /** App cliente. NULL indica sesión de plataforma (KeyGo UI). */
  private final ClientAppId clientAppId;
  private SessionStatus status;
  private final Instant expiresAt;
  private Instant lastAccessedAt;
  private final String userAgent;
  private final String ipAddress;
  private final Instant createdAt;
  /** UUID (como String) de la clave RSA que firmó los tokens de apertura. Nullable para sesiones legacy. */
  private final String signingKeyId;

  private Session(
      SessionId id,
      UUID platformUserId,
      ClientAppId clientAppId,
      SessionStatus status,
      Instant expiresAt,
      Instant lastAccessedAt,
      String userAgent,
      String ipAddress,
      Instant createdAt,
      String signingKeyId) {
    this.id = id;
    this.platformUserId = platformUserId;
    this.clientAppId = clientAppId;
    this.status = status;
    this.expiresAt = expiresAt;
    this.lastAccessedAt = lastAccessedAt;
    this.userAgent = userAgent;
    this.ipAddress = ipAddress;
    this.createdAt = createdAt;
    this.signingKeyId = signingKeyId;
  }

  /**
   * Factory method: crea una sesión nueva en estado ACTIVE.
   *
   * @param platformUserId UUID del platform_user (nullable para MVP — tenant-only users)
   * @param clientAppId    app cliente (nullable — null = sesión de plataforma)
   * @param expiresAt      expiración de la sesión
   * @param now            instante de creación
   * @param userAgent      user-agent del cliente (nullable)
   * @param ipAddress      dirección IP del cliente (nullable)
   * @param signingKeyId   UUID de la clave RSA usada (nullable)
   * @return nueva sesión en estado ACTIVE
   */
  public static Session open(
      UUID platformUserId,
      ClientAppId clientAppId,
      Instant expiresAt,
      Instant now,
      String userAgent,
      String ipAddress,
      String signingKeyId) {
    return open(UUID.randomUUID(), platformUserId, clientAppId, expiresAt, now,
        userAgent, ipAddress, signingKeyId);
  }

  public static Session open(
      UUID preGeneratedId,
      UUID platformUserId,
      ClientAppId clientAppId,
      Instant expiresAt,
      Instant now,
      String userAgent,
      String ipAddress,
      String signingKeyId) {
    if (expiresAt == null) throw new IllegalArgumentException("ExpiresAt cannot be null");
    if (now == null) throw new IllegalArgumentException("Now cannot be null");

    return new Session(
        SessionId.from(preGeneratedId),
        platformUserId, clientAppId,
        SessionStatus.ACTIVE,
        expiresAt, now, userAgent, ipAddress, now,
        signingKeyId);
  }

  /** Reconstruye una sesión desde persistencia. */
  public static Session reconstitute(
      SessionId id,
      UUID platformUserId,
      ClientAppId clientAppId,
      SessionStatus status,
      Instant expiresAt,
      Instant lastAccessedAt,
      String userAgent,
      String ipAddress,
      Instant createdAt,
      String signingKeyId) {
    return new Session(id, platformUserId, clientAppId, status, expiresAt,
        lastAccessedAt, userAgent, ipAddress, createdAt, signingKeyId);
  }

  /** @return true si es una sesión de plataforma (sin client app). */
  public boolean isPlatformSession() {
    return clientAppId == null;
  }

  /** @return true si la sesión está activa */
  public boolean isActive() {
    return status == SessionStatus.ACTIVE;
  }

  /**
   * Termina la sesión. Solo aplicable si está ACTIVE.
   *
   * @throws SessionInvalidStateException si la sesión no está ACTIVE
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
