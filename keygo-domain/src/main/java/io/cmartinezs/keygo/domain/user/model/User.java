package io.cmartinezs.keygo.domain.user.model;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import lombok.Builder;
import lombok.Getter;

/**
 * User domain entity — represents a human identity scoped to a specific tenant.
 * <p>Entidad de dominio User — representa una identidad humana con alcance dentro de un tenant.
 * Username and email are unique per tenant, not globally.
 * <p>El username y email son únicos por tenant, no globalmente.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class User {

  private final UserId id;
  private final TenantId tenantId;
  private final Username username;
  private final EmailAddress email;
  private PasswordHash passwordHash;
  private String firstName;
  private String lastName;
  private UserStatus status;

  @Builder
  private User(
      UserId id,
      TenantId tenantId,
      Username username,
      EmailAddress email,
      PasswordHash passwordHash,
      String firstName,
      String lastName,
      UserStatus status) {
    if (id == null) throw new IllegalArgumentException("User id cannot be null");
    if (tenantId == null) throw new IllegalArgumentException("User tenantId cannot be null");
    if (username == null) throw new IllegalArgumentException("User username cannot be null");
    if (email == null) throw new IllegalArgumentException("User email cannot be null");
    if (passwordHash == null) throw new IllegalArgumentException("User passwordHash cannot be null");
    if (status == null) throw new IllegalArgumentException("User status cannot be null");

    this.id = id;
    this.tenantId = tenantId;
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.firstName = firstName;
    this.lastName = lastName;
    this.status = status;
  }

  // ─── Domain behaviour ─────────────────────────────────────────────────────

  /**
   * Returns true if the user account is active.
   * <p>Retorna true si la cuenta de usuario está activa.
   */
  public boolean isActive() {
    return UserStatus.ACTIVE.equals(this.status);
  }

  /**
   * Returns true if the user account is pending email verification.
   * <p>Retorna true si la cuenta de usuario está pendiente de verificación de email.
   */
  public boolean isPending() {
    return UserStatus.PENDING.equals(this.status);
  }

  /**
   * Returns true if the user account is suspended.
   * <p>Retorna true si la cuenta de usuario está suspendida.
   */
  public boolean isSuspended() {
    return UserStatus.SUSPENDED.equals(this.status);
  }

  /**
   * Suspend this user account. Throws if already suspended.
   * <p>Suspende esta cuenta de usuario. Lanza excepción si ya estaba suspendida.
   * @throws IllegalStateException if the user is already suspended
   */
  public void suspend() {
    if (UserStatus.SUSPENDED.equals(this.status)) {
      throw new IllegalStateException("User '" + username.value() + "' is already suspended");
    }
    this.status = UserStatus.SUSPENDED;
  }

  /**
   * Reactivate a previously suspended user account.
   * <p>Reactiva una cuenta de usuario previamente suspendida.
   */
  public void activate() {
    this.status = UserStatus.ACTIVE;
  }

  /**
   * Update the user's password hash.
   * <p>Actualiza el hash de contraseña del usuario.
   * @param newPasswordHash the new hashed password
   */
  public void updatePassword(PasswordHash newPasswordHash) {
    if (newPasswordHash == null) {
      throw new IllegalArgumentException("New password hash cannot be null");
    }
    this.passwordHash = newPasswordHash;
  }

  /**
   * Update the user's display name fields.
   * <p>Actualiza los campos de nombre visible del usuario.
   * @param firstName new first name (may be null)
   * @param lastName  new last name (may be null)
   */
  public void updateName(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  @Override
  public String toString() {
    return "User{id=" + id + ", tenantId=" + tenantId + ", username=" + username + ", status=" + status + "}";
  }
}

