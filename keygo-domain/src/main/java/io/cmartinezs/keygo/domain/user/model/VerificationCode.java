package io.cmartinezs.keygo.domain.user.model;

import io.cmartinezs.keygo.domain.user.exception.VerificationCodeAlreadyUsedException;

import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de dominio unificado para códigos de verificación.
 *
 * <p>Consolida los anteriores {@code EmailVerification}, {@code PasswordResetCode} y
 * {@code PasswordRecoveryToken} en un solo modelo discriminado por {@link VerificationPurpose}.
 *
 * <p>Cada código tiene un TTL configurable y es de un solo uso.
 * La unicidad por usuario + propósito se garantiza a nivel de BD (partial UNIQUE index).
 *
 * @author cmartinezs
 * @version 1.0
 */
public class VerificationCode {

  private final UUID id;
  private final UserId userId;
  private final VerificationPurpose purpose;
  private final String code;
  private final Instant expiresAt;
  private Instant usedAt;
  private final Instant createdAt;

  private VerificationCode(
      UUID id,
      UserId userId,
      VerificationPurpose purpose,
      String code,
      Instant expiresAt,
      Instant usedAt,
      Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.purpose = purpose;
    this.code = code;
    this.expiresAt = expiresAt;
    this.usedAt = usedAt;
    this.createdAt = createdAt;
  }

  /**
   * Crea un nuevo código de verificación (no persistido aún).
   *
   * @param userId    identificador del usuario
   * @param purpose   propósito del código
   * @param code      código o token generado
   * @param expiresAt timestamp de expiración
   * @return nueva instancia con {@code id = null} (Hibernate genera el UUID al persistir), {@code usedAt = null}
   */
  public static VerificationCode create(
      UserId userId, VerificationPurpose purpose, String code, Instant expiresAt) {
    return new VerificationCode(null, userId, purpose, code, expiresAt, null, Instant.now());
  }

  /**
   * Reconstituye una instancia desde persistencia.
   */
  public static VerificationCode reconstitute(
      UUID id,
      UserId userId,
      VerificationPurpose purpose,
      String code,
      Instant expiresAt,
      Instant usedAt,
      Instant createdAt) {
    return new VerificationCode(id, userId, purpose, code, expiresAt, usedAt, createdAt);
  }

  /** {@code true} si el código ha expirado. */
  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  /** {@code true} si el código ya fue usado. */
  public boolean isUsed() {
    return usedAt != null;
  }

  /** {@code true} si el código es válido: no expirado y no usado. */
  public boolean isValid() {
    return !isExpired() && !isUsed();
  }

  /**
   * Marca este código como usado en el instante actual.
   *
   * @throws VerificationCodeAlreadyUsedException si ya fue usado
   */
  public void markUsed() {
    if (isUsed()) {
      throw new VerificationCodeAlreadyUsedException(purpose);
    }
    this.usedAt = Instant.now();
  }

  // ─── Getters ──────────────────────────────────────────────────────────────

  public UUID getId() { return id; }
  public UserId getUserId() { return userId; }
  public VerificationPurpose getPurpose() { return purpose; }
  public String getCode() { return code; }
  public Instant getExpiresAt() { return expiresAt; }
  public Instant getUsedAt() { return usedAt; }
  public Instant getCreatedAt() { return createdAt; }
}
