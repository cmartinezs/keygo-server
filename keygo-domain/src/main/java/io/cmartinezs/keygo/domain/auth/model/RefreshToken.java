package io.cmartinezs.keygo.domain.auth.model;

import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.time.Instant;
import lombok.Getter;

/**
 * Agregado: Refresh Token OAuth2.
 *
 * <p>El token plano NO se almacena aquí; solo el hash BCrypt del token.
 * La aplicación genera el token plano, lo entrega al cliente, y persiste únicamente el hash.
 *
 * <p>Implementa rotación de tokens: cada uso genera un nuevo refresh token (ACTIVE)
 * y marca el actual como USED.
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
  private final TenantId tenantId;
  private final ClientAppId clientAppId;
  private final UserId userId;
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
      TenantId tenantId,
      ClientAppId clientAppId,
      UserId userId,
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
    this.tenantId = tenantId;
    this.clientAppId = clientAppId;
    this.userId = userId;
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
   * @param tenantId     tenant propietario
   * @param clientAppId  app cliente
   * @param userId       usuario
   * @param sessionId    sesión asociada
   * @param scopes       scopes otorgados
   * @param expiresAt    fecha de expiración
   * @param now          instante de creación
   * @param signingKeyId UUID de la clave RSA usada (nullable)
   * @return nuevo RefreshToken en estado ACTIVE
   */
  public static RefreshToken issue(
      String tokenHash,
      TenantId tenantId,
      ClientAppId clientAppId,
      UserId userId,
      SessionId sessionId,
      String scopes,
      Instant expiresAt,
      Instant now,
      String signingKeyId) {
    if (tokenHash == null || tokenHash.isBlank()) throw new IllegalArgumentException("tokenHash cannot be null or blank");
    if (tenantId == null) throw new IllegalArgumentException("TenantId cannot be null");
    if (clientAppId == null) throw new IllegalArgumentException("ClientAppId cannot be null");
    if (userId == null) throw new IllegalArgumentException("UserId cannot be null");
    if (sessionId == null) throw new IllegalArgumentException("SessionId cannot be null");
    if (scopes == null) throw new IllegalArgumentException("Scopes cannot be null");
    if (expiresAt == null) throw new IllegalArgumentException("ExpiresAt cannot be null");
    if (now == null) throw new IllegalArgumentException("Now cannot be null");

    return new RefreshToken(
        RefreshTokenId.generate(),
        tokenHash,
        tenantId, clientAppId, userId, sessionId, scopes,
        RefreshTokenStatus.ACTIVE,
        expiresAt, now, null, null,
        signingKeyId);
  }

  /** Reconstruye un RefreshToken desde persistencia. */
  public static RefreshToken reconstitute(
      RefreshTokenId id,
      String tokenHash,
      TenantId tenantId,
      ClientAppId clientAppId,
      UserId userId,
      SessionId sessionId,
      String scopes,
      RefreshTokenStatus status,
      Instant expiresAt,
      Instant createdAt,
      Instant usedAt,
      RefreshTokenId replacedByTokenId,
      String signingKeyId) {
    return new RefreshToken(id, tokenHash, tenantId, clientAppId, userId, sessionId, scopes,
        status, expiresAt, createdAt, usedAt, replacedByTokenId, signingKeyId);
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

