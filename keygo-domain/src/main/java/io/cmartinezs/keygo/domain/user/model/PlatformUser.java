package io.cmartinezs.keygo.domain.user.model;

import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
import lombok.Builder;
import lombok.Getter;

/**
 * PlatformUser — global identity for the KeyGo platform.
 * <p>NOT scoped to any tenant. Email and username are globally unique.
 * <p>Identidad global de la plataforma KeyGo. No pertenece a ningún tenant.
 * Email y username son globalmente únicos.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class PlatformUser {

  private final UserId id;
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

  @Builder
  private PlatformUser(
      UserId id,
      Username username,
      EmailAddress email,
      PasswordHash passwordHash,
      String firstName,
      String lastName,
      UserStatus status,
      String phoneNumber,
      String locale,
      String zoneinfo,
      String profilePictureUrl) {
    if (username == null) throw new IllegalArgumentException("PlatformUser username cannot be null");
    if (email == null) throw new IllegalArgumentException("PlatformUser email cannot be null");
    if (passwordHash == null)
      throw new IllegalArgumentException("PlatformUser passwordHash cannot be null");
    if (status == null) throw new IllegalArgumentException("PlatformUser status cannot be null");

    this.id = id;
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
  }

  // ─── Domain behaviour ─────────────────────────────────────────────────────

  public boolean isActive() {
    return UserStatus.ACTIVE.equals(this.status);
  }

  public boolean isPending() {
    return UserStatus.PENDING.equals(this.status);
  }

  public boolean isSuspended() {
    return UserStatus.SUSPENDED.equals(this.status);
  }

  public boolean isResetPassword() {
    return UserStatus.RESET_PASSWORD.equals(this.status);
  }

  /**
   * Suspend this platform user account. Throws if already suspended.
   *
   * @throws UserSuspendedException if the user is already suspended
   */
  public void suspend() {
    if (UserStatus.SUSPENDED.equals(this.status)) {
      throw new UserSuspendedException(username.value());
    }
    this.status = UserStatus.SUSPENDED;
  }

  /** Reactivate a previously suspended platform user account. */
  public void activate() {
    this.status = UserStatus.ACTIVE;
  }

  /**
   * Update the platform user's password hash.
   *
   * @param newPasswordHash the new hashed password
   */
  public void updatePassword(PasswordHash newPasswordHash) {
    if (newPasswordHash == null) {
      throw new IllegalArgumentException("New password hash cannot be null");
    }
    this.passwordHash = newPasswordHash;
  }

  /**
   * Update the platform user's extended OIDC profile claims. All parameters are optional — pass
   * null to leave a field unchanged.
   */
  public void updateProfile(
      String firstName,
      String lastName,
      String phoneNumber,
      String locale,
      String zoneinfo,
      String profilePictureUrl) {
    if (firstName != null) this.firstName = firstName;
    if (lastName != null) this.lastName = lastName;
    if (phoneNumber != null) this.phoneNumber = phoneNumber;
    if (locale != null) this.locale = locale;
    if (zoneinfo != null) this.zoneinfo = zoneinfo;
    if (profilePictureUrl != null) this.profilePictureUrl = profilePictureUrl;
  }

  @Override
  public String toString() {
    return "PlatformUser{id=" + id + ", username=" + username + ", status=" + status + "}";
  }
}
