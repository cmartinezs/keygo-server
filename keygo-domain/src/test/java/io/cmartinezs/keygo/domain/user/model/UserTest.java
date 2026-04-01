package io.cmartinezs.keygo.domain.user.model;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

  private static final String VALID_USERNAME = "johndoe";
  private static final String VALID_EMAIL = "john.doe@example.com";
  private static final String VALID_HASH = "$2a$10$somehashedvalue";

  private User buildActiveUser() {
    return User.builder()
        .id(UserId.generate())
        .tenantId(TenantId.of(UUID.randomUUID()))
        .username(Username.of(VALID_USERNAME))
        .email(EmailAddress.of(VALID_EMAIL))
        .passwordHash(PasswordHash.of(VALID_HASH))
        .firstName("John")
        .lastName("Doe")
        .status(UserStatus.ACTIVE)
        .build();
  }

  @Test
  void buildsUserSuccessfully() {
    // When
    User user = buildActiveUser();

    // Then
    assertThat(user.getUsername().value()).isEqualTo(VALID_USERNAME);
    assertThat(user.getEmail().value()).isEqualTo(VALID_EMAIL);
    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(user.isActive()).isTrue();
    assertThat(user.isSuspended()).isFalse();
  }

  @Test
  void suspendChangesStatus() {
    // Given
    User user = buildActiveUser();

    // When
    user.suspend();

    // Then
    assertThat(user.isSuspended()).isTrue();
    assertThat(user.isActive()).isFalse();
  }

  @Test
  void suspendAlreadySuspendedThrows() {
    // Given
    User user = buildActiveUser();
    user.suspend();

    // When / Then
    assertThatThrownBy(user::suspend)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already suspended");
  }

  @Test
  void activateRestoresActiveStatus() {
    // Given
    User user = buildActiveUser();
    user.suspend();

    // When
    user.activate();

    // Then
    assertThat(user.isActive()).isTrue();
  }

  @Test
  void requirePasswordReset_setsResetPasswordStatus() {
    // Given
    User user = buildActiveUser();

    // When
    user.requirePasswordReset();

    // Then
    assertThat(user.isResetPassword()).isTrue();
    assertThat(user.isActive()).isFalse();
    assertThat(user.getStatus()).isEqualTo(UserStatus.RESET_PASSWORD);
  }

  @Test
  void builderWithResetPasswordStatus_succeeds() {
    // When
    User user = User.builder()
        .id(UserId.generate())
        .tenantId(TenantId.of(UUID.randomUUID()))
        .username(Username.of(VALID_USERNAME))
        .email(EmailAddress.of(VALID_EMAIL))
        .passwordHash(PasswordHash.of(VALID_HASH))
        .status(UserStatus.RESET_PASSWORD)
        .build();

    // Then
    assertThat(user.isResetPassword()).isTrue();
    assertThat(user.isActive()).isFalse();
    assertThat(user.isSuspended()).isFalse();
    assertThat(user.isPending()).isFalse();
  }

  @Test
  void updatePasswordReplacesHash() {
    // Given
    User user = buildActiveUser();
    PasswordHash newHash = PasswordHash.of("$2a$10$newhash");

    // When
    user.updatePassword(newHash);

    // Then
    assertThat(user.getPasswordHash().value()).isEqualTo("$2a$10$newhash");
  }

  @Test
  void updatePasswordWithNullThrows() {
    // Given
    User user = buildActiveUser();

    // When / Then
    assertThatThrownBy(() -> user.updatePassword(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateNameChangesFields() {
    // Given
    User user = buildActiveUser();

    // When
    user.updateName("Jane", "Smith");

    // Then
    assertThat(user.getFirstName()).isEqualTo("Jane");
    assertThat(user.getLastName()).isEqualTo("Smith");
  }

  @Test
  void isPending_whenStatusIsPending_returnsTrue() {
    // Given
    User user =
        User.builder()
            .id(UserId.generate())
            .tenantId(TenantId.of(UUID.randomUUID()))
            .username(Username.of(VALID_USERNAME))
            .email(EmailAddress.of(VALID_EMAIL))
            .passwordHash(PasswordHash.of(VALID_HASH))
            .status(UserStatus.PENDING)
            .build();

    // When / Then
    assertThat(user.isPending()).isTrue();
    assertThat(user.isActive()).isFalse();
  }

  @Test
  void updateProfile_withAllValues_updatesAllFields() {
    // Given
    User user = buildActiveUser();

    // When
    user.updateProfile(
        "Jane",
        "Smith",
        "+5215512345678",
        "es-MX",
        "America/Mexico_City",
        "https://cdn.example.com/jane.png",
        "1990-01-15",
        "https://janesmith.dev");

    // Then
    assertThat(user.getFirstName()).isEqualTo("Jane");
    assertThat(user.getLastName()).isEqualTo("Smith");
    assertThat(user.getPhoneNumber()).isEqualTo("+5215512345678");
    assertThat(user.getLocale()).isEqualTo("es-MX");
    assertThat(user.getZoneinfo()).isEqualTo("America/Mexico_City");
    assertThat(user.getProfilePictureUrl()).isEqualTo("https://cdn.example.com/jane.png");
    assertThat(user.getBirthdate()).isEqualTo("1990-01-15");
    assertThat(user.getWebsite()).isEqualTo("https://janesmith.dev");
  }

  @Test
  void updateProfile_withNullValues_keepsPreviousFields() {
    // Given
    User user = buildActiveUser();
    user.updateProfile(
        "Jane",
        "Smith",
        "+5215512345678",
        "es-MX",
        "America/Mexico_City",
        "https://cdn.example.com/jane.png",
        "1990-01-15",
        "https://janesmith.dev");

    // When
    user.updateProfile(null, null, null, null, null, null, null, null);

    // Then
    assertThat(user.getFirstName()).isEqualTo("Jane");
    assertThat(user.getLastName()).isEqualTo("Smith");
    assertThat(user.getPhoneNumber()).isEqualTo("+5215512345678");
    assertThat(user.getLocale()).isEqualTo("es-MX");
    assertThat(user.getZoneinfo()).isEqualTo("America/Mexico_City");
    assertThat(user.getProfilePictureUrl()).isEqualTo("https://cdn.example.com/jane.png");
    assertThat(user.getBirthdate()).isEqualTo("1990-01-15");
    assertThat(user.getWebsite()).isEqualTo("https://janesmith.dev");
  }

  @Test
  void builderWithNullTenantIdThrows() {
    // When / Then
    var builder = User.builder()
        .id(UserId.generate())
        .tenantId(null)
        .username(Username.of(VALID_USERNAME))
        .email(EmailAddress.of(VALID_EMAIL))
        .passwordHash(PasswordHash.of(VALID_HASH))
        .status(UserStatus.ACTIVE);

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("tenantId");
  }

  @Test
  void builderWithNullUsernameThrows() {
    // Given / When / Then
    var builder = User.builder()
        .id(UserId.generate())
        .tenantId(TenantId.of(UUID.randomUUID()))
        .username(null)
        .email(EmailAddress.of(VALID_EMAIL))
        .passwordHash(PasswordHash.of(VALID_HASH))
        .status(UserStatus.ACTIVE);

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("username");
  }

  @Test
  void builderWithNullEmailThrows() {
    // Given / When / Then
    var builder = User.builder()
        .id(UserId.generate())
        .tenantId(TenantId.of(UUID.randomUUID()))
        .username(Username.of(VALID_USERNAME))
        .email(null)
        .passwordHash(PasswordHash.of(VALID_HASH))
        .status(UserStatus.ACTIVE);

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("email");
  }

  @Test
  void builderWithNullPasswordHashThrows() {
    // Given / When / Then
    var builder = User.builder()
        .id(UserId.generate())
        .tenantId(TenantId.of(UUID.randomUUID()))
        .username(Username.of(VALID_USERNAME))
        .email(EmailAddress.of(VALID_EMAIL))
        .passwordHash(null)
        .status(UserStatus.ACTIVE);

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("passwordHash");
  }

  @Test
  void builderWithNullStatusThrows() {
    // Given / When / Then
    var builder = User.builder()
        .id(UserId.generate())
        .tenantId(TenantId.of(UUID.randomUUID()))
        .username(Username.of(VALID_USERNAME))
        .email(EmailAddress.of(VALID_EMAIL))
        .passwordHash(PasswordHash.of(VALID_HASH))
        .status(null);

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("status");
  }
}
