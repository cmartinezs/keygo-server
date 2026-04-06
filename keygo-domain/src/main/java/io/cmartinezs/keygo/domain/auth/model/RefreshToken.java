package io.cmartinezs.keygo.domain.auth.model;

import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

/**
 * Agregado: Refresh Token OAuth2.
 *
 * <p>El token plano NO se almacena aquí; solo el hash SHA-256 del token.
 * La aplicación genera el token plano, lo entrega al cliente, y persiste únicamente el hash.
 *
 * <p>Implementa rotación de tokens: cada uso genera un nuevo refresh token (ACTIVE)
 * y marca el actual como USED.
 *
 * <p>Modelo restructurado (RFC restructure-multitenant):
 * <ul>
 *   <li>{@code clientAppId} — nullable (null = RT de sesión de plataforma)
 *   <li>{@code tenantUserId} — nullable (contexto tenant para lookup rápido de roles en rotación)
 *   <li>{@code tenantId} y {@code userId} eliminados (derivables desde session)
 * </ul>
 *
 * <p>Invariantes:
 * <ul>
 *   <li>Solo un token ACTIVE puede ser marcado como USED
 *   <li>Un token USED o REVOKED no puede reutilizarse
 *   <li>El tokenHash nunca es nulo ni vacío
 * </ul>
 */
@Getter
public class RefreshToken {
  private final RefreshTokenId id;
  private final String tokenHash;
  /** App cliente. Nullable — null indica RT de sesión de plataforma. */
  private final ClientAppId clientAppId;
  /** UUID del tenant_user para lookup rápido de roles en rotación. Nullable. */
  private final UUID tenantUserId;
  private final SessionId sessionId;
  private final String scopes;
  private RefreshTokenStatus status;
  private final Instant expiresAt;
  private final Instant createdAt;
  private Instant usedAt;
  private RefreshTokenId replacedByTokenId;
  /** UUID (como String) de la clave RSA que firmó el access_token emitido. Nullable para RT legacy. */
  private final String signingKeyId;

  private RefreshToken(
      RefreshTokenId id,
      String tokenHash,
      ClientAppId clientAppId,
      UUID tenantUserId,
      SessionId sessionId,
      String scopes,
      RefreshTokenStatus status,
      Instant expiresAt,
      Instant createdAt,
      Instant usedAt,
      RefreshTokenId replacedByTokenId,
      String signingKeyId) {
    this.id = id;
    this.tokenHash = tokenHash;
    this.clientAppId = clientAppId;
    this.tenantUserId = tenantUserId;
    this.sessionId = sessionId;
    this.scopes = scopes;
    this.status = status;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
    this.usedAt = usedAt;
    this.replacedByTokenId = replacedByTokenId;
    this.signingKeyId = signingKeyId;
  }

  /**
   * Factory method: crea un nuevo refresh token en estado ACTIVE.
   *
   * @param tokenHash    hash SHA-256 del token plano
   * @param clientAppId  app cliente (nullable — null para sesiones de plataforma)
   * @param tenantUserId UUID del tenant_user (nullable — null para sesiones de plataforma)
   * @param sessionId    sesión asociada
   * @param scopes       scopes otorgados
   * @param expiresAt    fecha de expiración
   * @param now          instante de creación
   * @param signingKeyId UUID de la clave RSA usada (nullable)
   * @return nuevo RefreshToken en estado ACTIVE
   */
  public static RefreshToken issue(
      String tokenHash,
      ClientAppId clientAppId,
      UUID tenantUserId,
      SessionId sessionId,
      String scopes,
      Instant expiresAt,
      Instant now,
      String signingKeyId) {
    if (tokenHash == null || tokenHash.isBlank()) throw new IllegalArgumentException("tokenHash cannot be null or blank");
    if (sessionId == null) throw new IllegalArgumentException("SessionId cannot be null");
    if (scopes == null) throw new IllegalArgumentException("Scopes cannot be null");
    if (expiresAt == null) throw new IllegalArgumentException("ExpiresAt cannot be null");
    if (now == null) throw new IllegalArgumentException("Now cannot be null");

    return new RefreshToken(
        RefreshTokenId.generate(),
        tokenHash,
        clientAppId, tenantUserId, sessionId, scopes,
        RefreshTokenStatus.ACTIVE,
        expiresAt, now, null, null,
        signingKeyId);
  }

  /** Reconstruye un RefreshToken desde persistencia. */
  public static RefreshToken reconstitute(
      RefreshTokenId id,
      String tokenHash,
      ClientAppId clientAppId,
      UUID tenantUserId,
      SessionId sessionId,
      String scopes,
      RefreshTokenStatus status,
      Instant expiresAt,
      Instant createdAt,
      Instant usedAt,
      RefreshTokenId replacedByTokenId,
      String signingKeyId) {
    return new RefreshToken(id, tokenHash, clientAppId, tenantUserId, sessionId, scopes,
        status, expiresAt, createdAt, usedAt, replacedByTokenId, signingKeyId);
  }

  /** @return true si es un RT de sesión de plataforma (sin client app). */
  public boolean isPlatformToken() {
    return clientAppId == null;
  }

  /** @return true si el token ha expirado */
  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  /** @return true si el token puede usarse (ACTIVE y no expirado) */
  public boolean isValid() {
    return status == RefreshTokenStatus.ACTIVE && !isExpired();
  }

  /**
   * Marca el token como USED tras una rotación exitosa.
   *
   * @param usedAt          instante de uso
   * @param replacedById    ID del nuevo token que lo reemplaza
   * @throws InvalidRefreshTokenException si el token no está ACTIVE
   */
  public void markAsUsed(Instant usedAt, RefreshTokenId replacedById) {
    if (status != RefreshTokenStatus.ACTIVE) {
      throw new InvalidRefreshTokenException(
          "Cannot mark as used a refresh token that is not ACTIVE. Current status: " + status);
    }
    this.status = RefreshTokenStatus.USED;
    this.usedAt = usedAt;
    this.replacedByTokenId = replacedById;
  }

  /** Revoca el token. Idempotente si ya está REVOKED. */
  public void revoke() {
    if (status == RefreshTokenStatus.USED) {
      throw new InvalidRefreshTokenException("Cannot revoke a refresh token that has already been USED");
    }
    this.status = RefreshTokenStatus.REVOKED;
  }
}

