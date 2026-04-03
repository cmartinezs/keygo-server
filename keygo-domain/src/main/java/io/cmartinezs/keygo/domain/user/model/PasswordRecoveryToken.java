package io.cmartinezs.keygo.domain.user.model;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;

import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de dominio para tokens de recuperación de contraseña self-service.
 *
 * <p>Representa un token de un solo uso con TTL de 30 minutos.
 * La unicidad por usuario se garantiza a nivel de base de datos ({@code UNIQUE(tenant_user_id)}).
 *
 * @author cmartinezs
 * @version 1.0
 */
public class PasswordRecoveryToken {

  private final UUID id;
  private final UserId userId;
  private final TenantId tenantId;
  private final String token;
  private final Instant expiresAt;
  private Instant usedAt;
  private final Instant createdAt;

  private PasswordRecoveryToken(
      UUID id,
      UserId userId,
      TenantId tenantId,
      String token,
      Instant expiresAt,
      Instant usedAt,
      Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.tenantId = tenantId;
    this.token = token;
    this.expiresAt = expiresAt;
    this.usedAt = usedAt;
    this.createdAt = createdAt;
  }

  /**
   * Crea un nuevo token de recuperación (no persistido aún).
   */
  public static PasswordRecoveryToken create(
      UserId userId, TenantId tenantId, String token, Instant expiresAt) {
    return new PasswordRecoveryToken(
        null, userId, tenantId, token, expiresAt, null, Instant.now());
  }

  /**
   * Reconstituye un token desde persistencia.
   */
  public static PasswordRecoveryToken reconstitute(
      UUID id, UserId userId, TenantId tenantId,
      String token, Instant expiresAt, Instant usedAt, Instant createdAt) {
    return new PasswordRecoveryToken(id, userId, tenantId, token, expiresAt, usedAt, createdAt);
  }

  /** Returns true if the token has expired (expiresAt is in the past). */
  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  /** Returns true if the token has already been used. */
  public boolean isUsed() {
    return usedAt != null;
  }

  public UUID getId() { return id; }
  public UserId getUserId() { return userId; }
  public TenantId getTenantId() { return tenantId; }
  public String getToken() { return token; }
  public Instant getExpiresAt() { return expiresAt; }
  public Instant getUsedAt() { return usedAt; }
  public Instant getCreatedAt() { return createdAt; }
}
