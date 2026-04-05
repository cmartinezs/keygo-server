package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.CreateUserCommand;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.DuplicateUserException;
import io.cmartinezs.keygo.domain.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String USERNAME = "johndoe";
  private static final String EMAIL = "john@acme.com";
  private static final String RAW_PASSWORD = "SecurePass123!";  // 14 chars: uppercase, lowercase, digit, special
  private static final String HASHED_PASSWORD = "$2a$10$hash";

  @Mock TenantRepositoryPort tenantRepositoryPort;
  @Mock UserRepositoryPort userRepositoryPort;
  @Mock PasswordHasherPort passwordHasherPort;

  private CreateUserUseCase useCase;
  private Tenant activeTenant;

  @BeforeEach
  void setUp() {
    useCase = new CreateUserUseCase(tenantRepositoryPort, userRepositoryPort, passwordHasherPort);
    activeTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME Corp")
        .ownerEmail("owner@acme.com")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  @Test
  void createsUserSuccessfully() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.existsByTenantIdAndEmail(any(), any())).thenReturn(false);
    when(userRepositoryPort.existsByTenantIdAndUsername(any(), any())).thenReturn(false);
    when(passwordHasherPort.hash(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    CreateUserCommand command = new CreateUserCommand(TENANT_SLUG, USERNAME, EMAIL, RAW_PASSWORD, "John", "Doe");

    // When
    User user = useCase.execute(command);

    // Then
    assertThat(user.getUsername().value()).isEqualTo(USERNAME);
    assertThat(user.getEmail().value()).isEqualTo(EMAIL);
    assertThat(user.getPasswordHash().value()).isEqualTo(HASHED_PASSWORD);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepositoryPort).save(captor.capture());
    assertThat(captor.getValue().getFirstName()).isEqualTo("John");
  }

  @Test
  void throwsWhenTenantNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());
    CreateUserCommand command = new CreateUserCommand(TENANT_SLUG, USERNAME, EMAIL, RAW_PASSWORD, null, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  void throwsWhenTenantSuspended() {
    // Given
    Tenant suspended = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME Corp").ownerEmail("o@acme.com")
        .status(TenantStatus.SUSPENDED).build();
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(suspended));
    CreateUserCommand command = new CreateUserCommand(TENANT_SLUG, USERNAME, EMAIL, RAW_PASSWORD, null, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TenantSuspendedException.class);
  }

  @Test
  void throwsWhenEmailAlreadyExists() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.existsByTenantIdAndEmail(any(), any())).thenReturn(true);
    CreateUserCommand command = new CreateUserCommand(TENANT_SLUG, USERNAME, EMAIL, RAW_PASSWORD, null, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateUserException.class)
        .hasMessageContaining("email");
  }

  @Test
  void throwsWhenUsernameAlreadyExists() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.existsByTenantIdAndEmail(any(), any())).thenReturn(false);
    when(userRepositoryPort.existsByTenantIdAndUsername(any(), any())).thenReturn(true);
    CreateUserCommand command = new CreateUserCommand(TENANT_SLUG, USERNAME, EMAIL, RAW_PASSWORD, null, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateUserException.class)
        .hasMessageContaining("username");
  }
}

