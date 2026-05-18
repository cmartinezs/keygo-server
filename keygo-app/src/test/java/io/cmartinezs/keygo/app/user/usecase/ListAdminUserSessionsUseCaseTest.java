package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.auth.model.SessionStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAdminUserSessionsUseCaseTest {

  private static final String TENANT_SLUG = "acme";

  @Mock TenantRepositoryPort tenantRepository;
  @Mock UserRepositoryPort userRepository;
  @Mock SessionRepositoryPort sessionRepository;

  private ListAdminUserSessionsUseCase useCase;
  private Tenant tenant;
  private User user;
  private UUID tenantIdValue;
  private UUID userIdValue;

  @BeforeEach
  void setUp() {
    useCase = new ListAdminUserSessionsUseCase(tenantRepository, userRepository, sessionRepository);

    tenantIdValue = UUID.randomUUID();
    userIdValue = UUID.randomUUID();

    tenant = Tenant.builder()
        .id(TenantId.of(tenantIdValue))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME")
        .status(TenantStatus.ACTIVE)
        .build();

    user = User.builder()
        .id(UserId.of(userIdValue))
        .tenantId(TenantId.of(tenantIdValue))
        .username(Username.of("johndoe"))
        .email(EmailAddress.of("john@acme.com"))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .firstName("John").lastName("Doe")
        .status(UserStatus.ACTIVE)
        .build();
  }

  @Test
  void execute_shouldReturnSessionsForUser() {
    // Given
    var session = Session.reconstitute(
        SessionId.from(UUID.randomUUID()), userIdValue, ClientAppId.of(UUID.randomUUID()),
        SessionStatus.ACTIVE,
        Instant.now().plusSeconds(3600), Instant.now(), "Mozilla/5.0", "192.168.1.1", Instant.now(), null);

    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));
    when(sessionRepository.findAllByTenantUserIdAndTenantId(any(), any())).thenReturn(List.of(session));

    // When
    var result = useCase.execute(TENANT_SLUG, userIdValue.toString());

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).status()).isEqualTo("ACTIVE");
    assertThat(result.get(0).isCurrent()).isFalse();
  }

  @Test
  void execute_shouldEnforceTenantIsolation() {
    // Given
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));
    when(sessionRepository.findAllByTenantUserIdAndTenantId(any(), any())).thenReturn(List.of());

    // When
    useCase.execute(TENANT_SLUG, userIdValue.toString());

    // Then: verify queries use correct tenantUserId and tenantId
    ArgumentCaptor<UUID> tenantUserIdCaptor = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<UUID> tenantIdCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(sessionRepository).findAllByTenantUserIdAndTenantId(
        tenantUserIdCaptor.capture(), tenantIdCaptor.capture());

    assertThat(tenantUserIdCaptor.getValue()).isEqualTo(userIdValue);
    assertThat(tenantIdCaptor.getValue()).isEqualTo(tenantIdValue);
  }

  @Test
  void execute_shouldReturnEmptyList_whenNoSessions() {
    // Given
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));
    when(sessionRepository.findAllByTenantUserIdAndTenantId(any(), any())).thenReturn(List.of());

    // When
    var result = useCase.execute(TENANT_SLUG, userIdValue.toString());

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void execute_shouldThrowTenantNotFoundException_whenTenantNotFound() {
    // Given
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, userIdValue.toString()))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  void execute_shouldThrowUserNotFoundException_whenUserNotFound() {
    // Given
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), eq(TenantId.of(tenantIdValue)))).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, userIdValue.toString()))
        .isInstanceOf(UserNotFoundException.class);
  }
}
