package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ForgotPasswordCommand;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String USER_EMAIL = "john@acme.com";

  @Mock TenantRepositoryPort tenantRepositoryPort;
  @Mock UserRepositoryPort userRepositoryPort;
  @Mock VerificationCodeRepositoryPort tokenRepositoryPort;
  @Mock EmailNotificationPort emailNotificationPort;

  private ForgotPasswordUseCase useCase;
  private Tenant activeTenant;
  private User activeUser;

  @BeforeEach
  void setUp() {
    useCase = new ForgotPasswordUseCase(
        tenantRepositoryPort, userRepositoryPort, tokenRepositoryPort, emailNotificationPort);

    activeTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME").ownerEmail("o@acme.com")
        .status(TenantStatus.ACTIVE).build();

    activeUser = User.builder()
        .id(UserId.generate())
        .tenantId(activeTenant.getId())
        .username(Username.of("johndoe"))
        .email(EmailAddress.of(USER_EMAIL))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .firstName("John").lastName("Doe")
        .status(UserStatus.ACTIVE).build();
  }

  @Test
  void forgotPassword_sendsEmailWhenUserExists() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(activeUser));
    when(tokenRepositoryPort.upsert(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    var result = useCase.execute(new ForgotPasswordCommand(TENANT_SLUG, USER_EMAIL));

    // Then
    assertThat(result.sent()).isTrue();
    verify(tokenRepositoryPort).upsert(any(VerificationCode.class));
    verify(emailNotificationPort).sendPasswordRecoveryEmail(
        anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void forgotPassword_returnsSentTrueWhenEmailNotFound_antiEnumeration() {
    // Given — email does not exist
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.empty());

    // When
    var result = useCase.execute(new ForgotPasswordCommand(TENANT_SLUG, "unknown@acme.com"));

    // Then — must return success silently (no error, no email)
    assertThat(result.sent()).isTrue();
    verify(tokenRepositoryPort, never()).upsert(any());
    verify(emailNotificationPort, never()).sendPasswordRecoveryEmail(any(), any(), any(), any());
  }

  @Test
  void forgotPassword_throwsWhenTenantNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(new ForgotPasswordCommand(TENANT_SLUG, USER_EMAIL)))
        .isInstanceOf(TenantNotFoundException.class);
  }
}
