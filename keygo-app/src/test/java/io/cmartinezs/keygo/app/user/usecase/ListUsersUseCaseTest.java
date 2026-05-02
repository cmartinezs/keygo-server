package io.cmartinezs.keygo.app.user.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.filter.UserFilter;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListUsersUseCase")
class ListUsersUseCaseTest {

  @Mock
  private TenantRepositoryPort tenantRepositoryPort;

  @Mock
  private UserRepositoryPort userRepositoryPort;

  private ListUsersUseCase useCase;
  private Tenant activeTenant;
  private TenantId tenantId;

  @BeforeEach
  void setUp() {
    useCase = new ListUsersUseCase(tenantRepositoryPort, userRepositoryPort);
    tenantId = TenantId.of(UUID.randomUUID());
    activeTenant = Tenant.builder()
        .id(tenantId)
        .name("Test Tenant")
        .slug(TenantSlug.of("test-tenant"))
        .status(TenantStatus.ACTIVE)
        .build();
  }

  @Test
  @DisplayName("execute: throws TenantNotFoundException when tenant not found")
  void execute_throwsTenantNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(any()))
        .thenReturn(Optional.empty());

    UserFilter filter = UserFilter.of(null, null, null, 0, 20, null, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute("unknown-slug", filter))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  @DisplayName("execute: returns empty PagedResult when no users exist")
  void execute_returnsEmptyPagedResult() {
    // Given
    when(tenantRepositoryPort.findBySlug(any()))
        .thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findAllPaged(eq(tenantId), any()))
        .thenReturn(PagedResult.of(List.of(), 0, 20, 0));

    UserFilter filter = UserFilter.of(null, null, null, 0, 20, null, null);

    // When
    PagedResult<User> result = useCase.execute("test-tenant", filter);

    // Then
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
    assertThat(result.getPage()).isZero();
    assertThat(result.getSize()).isEqualTo(20);
  }

  @Test
  @DisplayName("execute: returns PagedResult with users")
  void execute_returnsPagedResultWithUsers() {
    // Given
    User user1 = User.builder()
        .id(UserId.of(UUID.randomUUID().toString()))
        .tenantId(tenantId)
        .username(Username.of("john"))
        .email(EmailAddress.of("john@test.com"))
        .passwordHash(PasswordHash.of("hashed"))
        .status(UserStatus.ACTIVE)
        .build();

    User user2 = User.builder()
        .id(UserId.of(UUID.randomUUID().toString()))
        .tenantId(tenantId)
        .username(Username.of("jane"))
        .email(EmailAddress.of("jane@test.com"))
        .passwordHash(PasswordHash.of("hashed"))
        .status(UserStatus.ACTIVE)
        .build();

    PagedResult<User> expectedResult = PagedResult.of(
        List.of(user1, user2), 0, 20, 2);

    when(tenantRepositoryPort.findBySlug(any()))
        .thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findAllPaged(eq(tenantId), any()))
        .thenReturn(expectedResult);

    UserFilter filter = UserFilter.of(null, null, null, 0, 20, null, null);

    // When
    PagedResult<User> result = useCase.execute("test-tenant", filter);

    // Then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getTotalPages()).isEqualTo(1);
    assertThat(result.isLast()).isTrue();
  }

  @Test
  @DisplayName("execute: passes filter to repository")
  void execute_passesFilterToRepository() {
    // Given
    when(tenantRepositoryPort.findBySlug(any()))
        .thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findAllPaged(eq(tenantId), any()))
        .thenReturn(PagedResult.of(List.of(), 0, 20, 0));

    UserFilter filter = UserFilter.of(
        UserStatus.ACTIVE, "john", "test.com", 1, 10, "username", "DESC");

    // When
    useCase.execute("test-tenant", filter);

    // Then — verify the filter was passed through
    org.mockito.Mockito.verify(userRepositoryPort)
        .findAllPaged(eq(tenantId), eq(filter));
  }
}
