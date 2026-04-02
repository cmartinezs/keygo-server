package io.cmartinezs.keygo.domain.user.model;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.exception.EmailVerificationInvalidException;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing an email verification request for a tenant user.
 * <p>Modelo de dominio que representa una solicitud de verificación de email para un usuario de tenant.
 * A verification code is valid for 30 minutes from creation.
 * <p>Un código de verificación es válido por 30 minutos desde su creación.
 * @author cmartinezs
 * @version 1.0
 */
public class EmailVerification {

  private final UUID id;
  private final UserId userId;
  private final TenantId tenantId;
  private final String code;
  private final Instant expiresAt;
  private Instant usedAt;
  private final Instant createdAt;

  private EmailVerification(
      UUID id,
      UserId userId,
      TenantId tenantId,
      String code,
      Instant expiresAt,
      Instant usedAt,
      Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.tenantId = tenantId;
    this.code = code;
    this.expiresAt = expiresAt;
    this.usedAt = usedAt;
    this.createdAt = createdAt;
  }

  // ─── Factory methods ──────────────────────────────────────────────────────

  /**
   * Create a new (not-yet-used) email verification.
   * <p>Crea una nueva verificación de email (no utilizada aún).
   * @param userId    the user identifier
   * @param tenantId  the tenant identifier
   * @param code      the verification code (6 digits)
   * @param expiresAt when this code expires
   * @return the new EmailVerification
   */
  public static EmailVerification create(UserId userId, TenantId tenantId, String code, Instant expiresAt) {
    return new EmailVerification(UUID.randomUUID(), userId, tenantId, code, expiresAt, null, Instant.now());
  }

  /**
   * Reconstruct an existing email verification from persistence.
   * <p>Reconstruye una verificación de email existente desde persistencia.
   */
  public static EmailVerification reconstitute(
      UUID id,
      UserId userId,
      TenantId tenantId,
      String code,
      Instant expiresAt,
      Instant usedAt,
      Instant createdAt) {
    return new EmailVerification(id, userId, tenantId, code, expiresAt, usedAt, createdAt);
  }

  // ─── Getters ──────────────────────────────────────────────────────────────

  public UUID getId() { return id; }
  public UserId getUserId() { return userId; }
  public TenantId getTenantId() { return tenantId; }
  public String getCode() { return code; }
  public Instant getExpiresAt() { return expiresAt; }
  public Instant getUsedAt() { return usedAt; }
  public Instant getCreatedAt() { return createdAt; }

  // ─── Domain behaviour ─────────────────────────────────────────────────────

  /**
   * Returns true if this verification code has expired.
   * <p>Retorna true si este código de verificación ha expirado.
   */
  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  /**
   * Returns true if this verification code has already been used.
   * <p>Retorna true si este código de verificación ya fue utilizado.
   */
  public boolean isUsed() {
    return usedAt != null;
  }

  /**
   * Returns true if this code is valid: not expired and not yet used.
   * <p>Retorna true si este código es válido: no expirado y no utilizado.
   */
  public boolean isValid() {
    return !isExpired() && !isUsed();
  }

  /**
   * Mark this verification as used at the current instant.
   * <p>Marca esta verificación como utilizada en el instante actual.
   * @throws IllegalStateException if already used
   */
  public void markUsed() {
    if (isUsed()) {
      throw new EmailVerificationInvalidException(this.userId.value().toString());
    }
    this.usedAt = Instant.now();
  }
}

