package io.cmartinezs.keygo.app.user.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuspendActivateUserUseCase")
class SuspendActivateUserUseCaseTest {

  @Mock
  private TenantRepositoryPort tenantRepositoryPort;

  @Mock
  private UserRepositoryPort userRepositoryPort;

  private SuspendUserUseCase suspendUserUseCase;
  private ActivateUserUseCase activateUserUseCase;

  private final String TENANT_SLUG = "test-tenant";
  private final UUID USER_UUID = UUID.randomUUID();
  private final String USER_ID = USER_UUID.toString();

  private Tenant activeTenant;
  private User activeUser;

  @BeforeEach
  void setUp() {
    suspendUserUseCase =
        new SuspendUserUseCase(tenantRepositoryPort, userRepositoryPort);
    activateUserUseCase =
        new ActivateUserUseCase(tenantRepositoryPort, userRepositoryPort);

    activeTenant =
        Tenant.builder()
            .id(TenantId.generate())
            .slug(TenantSlug.of(TENANT_SLUG))
            .name("Test Tenant")
            .ownerEmail("owner@example.com")
            .status(TenantStatus.ACTIVE)
            .build();

    activeUser =
        User.builder()
            .id(UserId.of(USER_ID))
            .tenantId(activeTenant.getId())
            .username(Username.of("testuser"))
            .email(EmailAddress.of("user@example.com"))
            .passwordHash(PasswordHash.of("$2a$10$hash"))
            .firstName("Test")
            .lastName("User")
            .status(UserStatus.ACTIVE)
            .build();
  }

  // ── SuspendUserUseCase ──────────────────────────────────────────────────────

  @Test
  @DisplayName("suspendUser: should suspend an active user and persist the change")
  void suspendUser_success() {
    // Given
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(UserId.of(USER_ID), activeTenant.getId()))
        .thenReturn(Optional.of(activeUser));
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    User result = suspendUserUseCase.execute(TENANT_SLUG, USER_ID);

    // Then
    assertThat(result.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    verify(userRepositoryPort).save(activeUser);
  }

  @Test
  @DisplayName("suspendUser: should throw TenantNotFoundException when tenant does not exist")
  void suspendUser_throwsTenantNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> suspendUserUseCase.execute(TENANT_SLUG, USER_ID))
        .isInstanceOf(TenantNotFoundException.class)
        .hasMessageContaining(TENANT_SLUG);

    verify(userRepositoryPort, never()).findByIdAndTenantId(any(), any());
    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("suspendUser: should throw UserNotFoundException when user does not exist")
  void suspendUser_throwsUserNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(UserId.of(USER_ID), activeTenant.getId()))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> suspendUserUseCase.execute(TENANT_SLUG, USER_ID))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(USER_ID);

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("suspendUser: should throw UserSuspendedException when user is already suspended")
  void suspendUser_throwsUserSuspendedException() {
    // Given
    activeUser.suspend(); // already suspended
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(UserId.of(USER_ID), activeTenant.getId()))
        .thenReturn(Optional.of(activeUser));

    // When / Then
    assertThatThrownBy(() -> suspendUserUseCase.execute(TENANT_SLUG, USER_ID))
        .isInstanceOf(UserSuspendedException.class);

    verify(userRepositoryPort, never()).save(any());
  }

  // ── ActivateUserUseCase ─────────────────────────────────────────────────────

  @Test
  @DisplayName("activateUser: should activate a suspended user and persist the change")
  void activateUser_success() {
    // Given
    User suspendedUser = User.builder()
        .id(UserId.of(USER_ID))
        .tenantId(activeTenant.getId())
        .username(Username.of("testuser"))
        .email(EmailAddress.of("user@example.com"))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .firstName("Test")
        .lastName("User")
        .status(UserStatus.SUSPENDED)
        .build();

    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(UserId.of(USER_ID), activeTenant.getId()))
        .thenReturn(Optional.of(suspendedUser));
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    User result = activateUserUseCase.execute(TENANT_SLUG, USER_ID);

    // Then
    assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
    verify(userRepositoryPort).save(suspendedUser);
  }

  @Test
  @DisplayName("activateUser: should throw TenantNotFoundException when tenant does not exist")
  void activateUser_throwsTenantNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> activateUserUseCase.execute(TENANT_SLUG, USER_ID))
        .isInstanceOf(TenantNotFoundException.class)
        .hasMessageContaining(TENANT_SLUG);

    verify(userRepositoryPort, never()).findByIdAndTenantId(any(), any());
    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("activateUser: should throw UserNotFoundException when user does not exist")
  void activateUser_throwsUserNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(UserId.of(USER_ID), activeTenant.getId()))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> activateUserUseCase.execute(TENANT_SLUG, USER_ID))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(USER_ID);

    verify(userRepositoryPort, never()).save(any());
  }
}
