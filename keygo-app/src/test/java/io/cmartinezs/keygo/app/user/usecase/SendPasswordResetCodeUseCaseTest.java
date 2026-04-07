package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.SendPasswordResetCodeCommand;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.SendPasswordResetCodeResult;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para {@link SendPasswordResetCodeUseCase}.
 *
 * <p>Verifica que el use case genera, persiste y envía el código correctamente,
 * y que retorna el {@code requestId} de la solicitud persistida.
 */
@ExtendWith(MockitoExtension.class)
class SendPasswordResetCodeUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String USER_EMAIL  = "john@acme.com";

  @Mock TenantRepositoryPort            tenantRepositoryPort;
  @Mock UserRepositoryPort              userRepositoryPort;
  @Mock VerificationCodeRepositoryPort   codeRepositoryPort;
  @Mock EmailNotificationPort           emailNotificationPort;

  private SendPasswordResetCodeUseCase useCase;
  private Tenant activeTenant;
  private User   resetPasswordUser;
  private UserId userId;
  private UUID   persistedCodeId;

  @BeforeEach
  void setUp() {
    useCase = new SendPasswordResetCodeUseCase(
        tenantRepositoryPort, userRepositoryPort, codeRepositoryPort, emailNotificationPort);

    activeTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME").ownerEmail("o@acme.com")
        .status(TenantStatus.ACTIVE).build();

    userId = UserId.generate();
    resetPasswordUser = User.builder()
        .id(userId)
        .tenantId(activeTenant.getId())
        .username(Username.of("johndoe"))
        .email(EmailAddress.of(USER_EMAIL))
        .passwordHash(PasswordHash.of("$2a$10$temphash"))
        .firstName("John").lastName("Doe")
        .status(UserStatus.ACTIVE).build();
    resetPasswordUser.requirePasswordReset();

    persistedCodeId = UUID.randomUUID();
  }

  // ─── Happy path ───────────────────────────────────────────────────────────

  @Test
  void execute_byEmail_returnsRequestId() {
    // Given
    VerificationCode persisted = VerificationCode.reconstitute(
        persistedCodeId, userId, VerificationPurpose.PASSWORD_RESET, "123456",
        Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(codeRepositoryPort.upsert(any())).thenReturn(persisted);
    doNothing().when(emailNotificationPort).sendPasswordResetCodeEmail(any(), any(), any(), any(Integer.class));

    // When
    SendPasswordResetCodeResult result = useCase.execute(
        new SendPasswordResetCodeCommand(TENANT_SLUG, USER_EMAIL));

    // Then
    assertThat(result).isNotNull();
    assertThat(result.requestId()).isEqualTo(persistedCodeId);
    verify(codeRepositoryPort).upsert(any());
    verify(emailNotificationPort).sendPasswordResetCodeEmail(
        any(), any(), any(), any(Integer.class));
  }

  @Test
  void execute_byUsername_returnsRequestId() {
    // Given
    VerificationCode persisted = VerificationCode.reconstitute(
        persistedCodeId, userId, VerificationPurpose.PASSWORD_RESET, "654321",
        Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    // "johndoe" is not a valid email → tryFindByEmail throws IAE → caught → try username
    when(userRepositoryPort.findByTenantIdAndUsername(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(codeRepositoryPort.upsert(any())).thenReturn(persisted);
    doNothing().when(emailNotificationPort).sendPasswordResetCodeEmail(any(), any(), any(), any(Integer.class));

    // When
    SendPasswordResetCodeResult result = useCase.execute(
        new SendPasswordResetCodeCommand(TENANT_SLUG, "johndoe"));

    // Then
    assertThat(result.requestId()).isEqualTo(persistedCodeId);
    verify(userRepositoryPort).findByTenantIdAndUsername(any(), any());
  }

  @Test
  void execute_upsertReplacesExistingCode() {
    // Given — second call (upsert replaces old code with new UUID)
    UUID newCodeId = UUID.randomUUID();
    VerificationCode newPersisted = VerificationCode.reconstitute(
        newCodeId, userId, VerificationPurpose.PASSWORD_RESET, "000001",
        Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(codeRepositoryPort.upsert(any())).thenReturn(newPersisted);
    doNothing().when(emailNotificationPort).sendPasswordResetCodeEmail(any(), any(), any(), any(Integer.class));

    // When
    SendPasswordResetCodeResult result = useCase.execute(
        new SendPasswordResetCodeCommand(TENANT_SLUG, USER_EMAIL));

    // Then — returns the ID from the persisted (possibly replaced) code
    assertThat(result.requestId()).isEqualTo(newCodeId);
  }

  // ─── Error paths ──────────────────────────────────────────────────────────

  @Test
  void execute_throwsWhenTenantNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new SendPasswordResetCodeCommand(TENANT_SLUG, USER_EMAIL)))
        .isInstanceOf(TenantNotFoundException.class);

    verify(codeRepositoryPort, never()).upsert(any());
    verify(emailNotificationPort, never()).sendPasswordResetCodeEmail(any(), any(), any(), any(Integer.class));
  }

  @Test
  void execute_throwsWhenUserNotFound() {
    // Given — "unknown_user" is not a valid email (IAE caught in tryFindByEmail),
    //         and username lookup returns empty → UserNotFoundException
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndUsername(any(), any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new SendPasswordResetCodeCommand(TENANT_SLUG, "unknown_user")))
        .isInstanceOf(UserNotFoundException.class);

    verify(codeRepositoryPort, never()).upsert(any());
  }

  // ─── Code generation ─────────────────────────────────────────────────────

  @Test
  void generateCode_returns6DigitString() {
    for (int i = 0; i < 20; i++) {
      String code = useCase.generateCode();
      assertThat(code).hasSize(6).matches("\\d{6}");
    }
  }
}



