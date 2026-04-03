package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.filter.UserFilter;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetListUsersUseCaseTest {

  private static final String TENANT_SLUG = "acme";

  @Mock TenantRepositoryPort tenantRepositoryPort;
  @Mock UserRepositoryPort userRepositoryPort;

  private Tenant activeTenant;
  private User sampleUser;

  @BeforeEach
  void setUp() {
    activeTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME Corp").ownerEmail("owner@acme.com")
        .status(TenantStatus.ACTIVE).build();

    sampleUser = User.builder()
        .id(UserId.generate())
        .tenantId(activeTenant.getId())
        .username(Username.of("johndoe"))
        .email(EmailAddress.of("john@acme.com"))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .status(UserStatus.ACTIVE).build();
  }

  // ─── GetUserUseCase ───────────────────────────────────────────────────────

  @Test
  void getUserReturnsUser() {
    // Given
    GetUserUseCase uc = new GetUserUseCase(tenantRepositoryPort, userRepositoryPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(sampleUser));

    // When
    User result = uc.execute(TENANT_SLUG, sampleUser.getId().toString());

    // Then
    assertThat(result.getUsername().value()).isEqualTo("johndoe");
  }

  @Test
  void getUserThrowsTenantNotFound() {
    // Given
    GetUserUseCase uc = new GetUserUseCase(tenantRepositoryPort, userRepositoryPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());
    String userId = UUID.randomUUID().toString();

    // When / Then
    assertThatThrownBy(() -> uc.execute(TENANT_SLUG, userId))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  void getUserThrowsUserNotFound() {
    // Given
    GetUserUseCase uc = new GetUserUseCase(tenantRepositoryPort, userRepositoryPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(any(), any())).thenReturn(Optional.empty());
    String userId = UUID.randomUUID().toString();

    // When / Then
    assertThatThrownBy(() -> uc.execute(TENANT_SLUG, userId))
        .isInstanceOf(UserNotFoundException.class);
  }

  // ─── ListUsersUseCase ─────────────────────────────────────────────────────

  @Test
  void listUsersReturnsAll() {
    // Given
    ListUsersUseCase uc = new ListUsersUseCase(tenantRepositoryPort, userRepositoryPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    PagedResult<User> pagedResult = PagedResult.of(List.of(sampleUser), 0, 20, 1);
    when(userRepositoryPort.findAllPaged(eq(activeTenant.getId()), any())).thenReturn(pagedResult);

    // When
    UserFilter filter = UserFilter.of(null, null, null, 0, 20, null, null);
    PagedResult<User> result = uc.execute(TENANT_SLUG, filter);

    // Then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  void listUsersThrowsTenantNotFound() {
    // Given
    ListUsersUseCase uc = new ListUsersUseCase(tenantRepositoryPort, userRepositoryPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());

    // When / Then
    UserFilter filter = UserFilter.of(null, null, null, 0, 20, null, null);
    assertThatThrownBy(() -> uc.execute(TENANT_SLUG, filter))
        .isInstanceOf(TenantNotFoundException.class);
  }
}

