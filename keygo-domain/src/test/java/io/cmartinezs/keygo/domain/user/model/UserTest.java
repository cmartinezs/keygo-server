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
  void builderWithNullIdThrows() {
    // When / Then
    var builder = User.builder()
        .id(null)
        .tenantId(TenantId.of(UUID.randomUUID()))
        .username(Username.of(VALID_USERNAME))
        .email(EmailAddress.of(VALID_EMAIL))
        .passwordHash(PasswordHash.of(VALID_HASH))
        .status(UserStatus.ACTIVE);

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("id");
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
}

