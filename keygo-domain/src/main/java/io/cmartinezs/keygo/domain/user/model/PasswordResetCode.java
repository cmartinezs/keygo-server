package io.cmartinezs.keygo.domain.user.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de dominio para códigos de verificación del flujo RESET_PASSWORD.
 *
 * <p>Cuando un usuario con {@code status=RESET_PASSWORD} intenta iniciar sesión con
 * credenciales válidas, se genera un código de 6 dígitos con TTL de 15 minutos
 * que debe presentar junto a la nueva contraseña para completar el cambio.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class PasswordResetCode {

  private final UUID id;
  private final UserId userId;
  private final String code;
  private final Instant expiresAt;
  private Instant usedAt;
  private final Instant createdAt;

  private PasswordResetCode(
      UUID id,
      UserId userId,
      String code,
      Instant expiresAt,
      Instant usedAt,
      Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.code = code;
    this.expiresAt = expiresAt;
    this.usedAt = usedAt;
    this.createdAt = createdAt;
  }

  /** Crea un nuevo código (no persistido aún). */
  public static PasswordResetCode create(UserId userId, String code, Instant expiresAt) {
    return new PasswordResetCode(null, userId, code, expiresAt, null, Instant.now());
  }

  /** Reconstituye desde persistencia. */
  public static PasswordResetCode reconstitute(
      UUID id, UserId userId, String code,
      Instant expiresAt, Instant usedAt, Instant createdAt) {
    return new PasswordResetCode(id, userId, code, expiresAt, usedAt, createdAt);
  }

  /** {@code true} si el código ha expirado. */
  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  /** {@code true} si el código ya fue usado. */
  public boolean isUsed() {
    return usedAt != null;
  }

  public UUID getId() { return id; }
  public UserId getUserId() { return userId; }
  public String getCode() { return code; }
  public Instant getExpiresAt() { return expiresAt; }
  public Instant getUsedAt() { return usedAt; }
  public Instant getCreatedAt() { return createdAt; }
}

