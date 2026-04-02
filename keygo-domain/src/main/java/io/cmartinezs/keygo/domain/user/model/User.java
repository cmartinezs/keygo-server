package io.cmartinezs.keygo.domain.user.model;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
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

  // ─── OIDC extended profile claims (OIDC §5.3) ────────────────────────────
  /** OIDC phone_number — "phone" scope */
  private String phoneNumber;
  /** OIDC locale — BCP47 tag, e.g. "es-MX" — "profile" scope */
  private String locale;
  /** OIDC zoneinfo — tz database, e.g. "America/Mexico_City" — "profile" scope */
  private String zoneinfo;
  /** OIDC picture — external URL — "profile" scope */
  private String profilePictureUrl;
  /** OIDC birthdate — ISO 8601 date string, e.g. "1990-01-15" — "profile" scope */
  private String birthdate;
  /** OIDC website — URL — "profile" scope */
  private String website;

  @Builder
  private User(
      UserId id,
      TenantId tenantId,
      Username username,
      EmailAddress email,
      PasswordHash passwordHash,
      String firstName,
      String lastName,
      UserStatus status,
      String phoneNumber,
      String locale,
      String zoneinfo,
      String profilePictureUrl,
      String birthdate,
      String website) {
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
    this.phoneNumber = phoneNumber;
    this.locale = locale;
    this.zoneinfo = zoneinfo;
    this.profilePictureUrl = profilePictureUrl;
    this.birthdate = birthdate;
    this.website = website;
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
   * Returns true if the user was provisioned with a temporary password and must reset it.
   * <p>Retorna true si el usuario fue aprovisionado con contraseña temporal y debe resetearla.
   */
  public boolean isResetPassword() {
    return UserStatus.RESET_PASSWORD.equals(this.status);
  }

  /**
   * Mark this user as requiring a password reset (provisioned with a temporary password).
   * <p>Marca a este usuario como requiriendo un cambio de contraseña (aprovisionado con contraseña temporal).
   */
  public void requirePasswordReset() {
    this.status = UserStatus.RESET_PASSWORD;
  }

  /**
   * Suspend this user account. Throws if already suspended.
   * <p>Suspende esta cuenta de usuario. Lanza excepción si ya estaba suspendida.
   * @throws IllegalStateException if the user is already suspended
   */
  public void suspend() {
    if (UserStatus.SUSPENDED.equals(this.status)) {
      throw new UserSuspendedException(username.value());
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

  /**
   * Update the user's extended OIDC profile claims.
   * <p>Actualiza los claims de perfil OIDC extendidos del usuario.
   * All parameters are optional — pass null to leave a field unchanged, pass empty string to clear it.
   * <p>Todos los parámetros son opcionales — pasar null deja el campo sin cambio, pasar string vacío lo borra.
   *
   * @param firstName         new first name (null = no change)
   * @param lastName          new last name (null = no change)
   * @param phoneNumber       OIDC phone_number (null = no change)
   * @param locale            BCP47 locale (null = no change)
   * @param zoneinfo          tz database zoneinfo (null = no change)
   * @param profilePictureUrl external picture URL (null = no change)
   * @param birthdate         ISO 8601 date (null = no change)
   * @param website           website URL (null = no change)
   */
  public void updateProfile(
      String firstName,
      String lastName,
      String phoneNumber,
      String locale,
      String zoneinfo,
      String profilePictureUrl,
      String birthdate,
      String website) {
    if (firstName != null)         this.firstName = firstName;
    if (lastName != null)          this.lastName = lastName;
    if (phoneNumber != null)       this.phoneNumber = phoneNumber;
    if (locale != null)            this.locale = locale;
    if (zoneinfo != null)          this.zoneinfo = zoneinfo;
    if (profilePictureUrl != null) this.profilePictureUrl = profilePictureUrl;
    if (birthdate != null)         this.birthdate = birthdate;
    if (website != null)           this.website = website;
  }

  @Override
  public String toString() {
    return "User{id=" + id + ", tenantId=" + tenantId + ", username=" + username + ", status=" + status + "}";
  }
}

