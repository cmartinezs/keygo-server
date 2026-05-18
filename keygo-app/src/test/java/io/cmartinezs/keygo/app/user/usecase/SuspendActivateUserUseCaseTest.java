package io.cmartinezs.keygo.app.user.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserStatusActionResult;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
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

  @Mock private TenantRepositoryPort tenantRepositoryPort;
  @Mock private UserRepositoryPort userRepositoryPort;

  private SuspendUserUseCase suspendUserUseCase;
  private ActivateUserUseCase activateUserUseCase;

  private static final String TENANT_SLUG = "test-tenant";
  private static final UUID USER_UUID = UUID.randomUUID();
  private static final String USER_ID = USER_UUID.toString();

  private Tenant activeTenant;
  private User activeUser;

  @BeforeEach
  void setUp() {
    suspendUserUseCase = new SuspendUserUseCase(tenantRepositoryPort, userRepositoryPort);
    activateUserUseCase = new ActivateUserUseCase(tenantRepositoryPort, userRepositoryPort);

    activeTenant = Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Test Tenant")
        .status(TenantStatus.ACTIVE)
        .build();

    activeUser = User.builder()
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
  @DisplayName("suspendUser: active user → SUSPENDED, idempotent=false")
  void suspendUser_success() {
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(UserId.of(USER_ID), activeTenant.getId())).thenReturn(Optional.of(activeUser));
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    UserStatusActionResult result = suspendUserUseCase.execute(TENANT_SLUG, USER_ID);

    assertThat(result.previousStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(result.currentStatus()).isEqualTo(UserStatus.SUSPENDED);
    assertThat(result.idempotent()).isFalse();
    assertThat(result.userId()).isEqualTo(USER_UUID);
    verify(userRepositoryPort).save(activeUser);
  }

  @Test
  @DisplayName("suspendUser: already SUSPENDED → idempotent=true, no persist")
  void suspendUser_alreadySuspended_isIdempotent() {
    User suspendedUser = User.builder()
        .id(UserId.of(USER_ID))
        .tenantId(activeTenant.getId())
        .username(Username.of("testuser"))
        .email(EmailAddress.of("user@example.com"))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .status(UserStatus.SUSPENDED)
        .build();

    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(UserId.of(USER_ID), activeTenant.getId())).thenReturn(Optional.of(suspendedUser));

    UserStatusActionResult result = suspendUserUseCase.execute(TENANT_SLUG, USER_ID);

    assertThat(result.previousStatus()).isEqualTo(UserStatus.SUSPENDED);
    assertThat(result.currentStatus()).isEqualTo(UserStatus.SUSPENDED);
    assertThat(result.idempotent()).isTrue();
    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("suspendUser: tenant not found → TenantNotFoundException")
  void suspendUser_throwsTenantNotFound() {
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> suspendUserUseCase.execute(TENANT_SLUG, USER_ID))
        .isInstanceOf(TenantNotFoundException.class)
        .hasMessageContaining(TENANT_SLUG);

    verify(userRepositoryPort, never()).findByIdAndTenantId(any(), any());
    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("suspendUser: user not found → UserNotFoundException")
  void suspendUser_throwsUserNotFound() {
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(UserId.of(USER_ID), activeTenant.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> suspendUserUseCase.execute(TENANT_SLUG, USER_ID))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(USER_ID);

    verify(userRepositoryPort, never()).save(any());
  }

  // ── ActivateUserUseCase ─────────────────────────────────────────────────────

  @Test
  @DisplayName("activateUser: suspended user → ACTIVE, idempotent=false")
  void activateUser_success() {
    User suspendedUser = User.builder()
        .id(UserId.of(USER_ID))
        .tenantId(activeTenant.getId())
        .username(Username.of("testuser"))
        .email(EmailAddress.of("user@example.com"))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .status(UserStatus.SUSPENDED)
        .build();

    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(UserId.of(USER_ID), activeTenant.getId())).thenReturn(Optional.of(suspendedUser));
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    UserStatusActionResult result = activateUserUseCase.execute(TENANT_SLUG, USER_ID);

    assertThat(result.previousStatus()).isEqualTo(UserStatus.SUSPENDED);
    assertThat(result.currentStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(result.idempotent()).isFalse();
    assertThat(result.userId()).isEqualTo(USER_UUID);
    verify(userRepositoryPort).save(suspendedUser);
  }

  @Test
  @DisplayName("activateUser: already ACTIVE → idempotent=true, no persist")
  void activateUser_alreadyActive_isIdempotent() {
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(UserId.of(USER_ID), activeTenant.getId())).thenReturn(Optional.of(activeUser));

    UserStatusActionResult result = activateUserUseCase.execute(TENANT_SLUG, USER_ID);

    assertThat(result.previousStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(result.currentStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(result.idempotent()).isTrue();
    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("activateUser: tenant not found → TenantNotFoundException")
  void activateUser_throwsTenantNotFound() {
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> activateUserUseCase.execute(TENANT_SLUG, USER_ID))
        .isInstanceOf(TenantNotFoundException.class)
        .hasMessageContaining(TENANT_SLUG);

    verify(userRepositoryPort, never()).findByIdAndTenantId(any(), any());
    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("activateUser: user not found → UserNotFoundException")
  void activateUser_throwsUserNotFound() {
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(UserId.of(USER_ID), activeTenant.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> activateUserUseCase.execute(TENANT_SLUG, USER_ID))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(USER_ID);

    verify(userRepositoryPort, never()).save(any());
  }
}
