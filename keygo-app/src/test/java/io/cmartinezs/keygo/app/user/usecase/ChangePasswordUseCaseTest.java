package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ChangePasswordCommand;
import io.cmartinezs.keygo.app.user.exception.IncorrectCurrentPasswordException;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.ChangePasswordResult;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.user.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para ChangePasswordUseCase.
 */
@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String BEARER_TOKEN = "valid.jwt.token";
  private static final String CURRENT_RAW = "OldPassword1!";
  private static final String CURRENT_HASH = "$2a$10$oldhash";
  private static final String NEW_STRONG_PASSWORD = "NewStr0ng!Pass#24";
  private static final String NEW_HASH = "$2a$10$newhash";

  @Mock SigningKeyRepositoryPort signingKeyRepository;
  @Mock AccessTokenVerifierPort accessTokenVerifier;
  @Mock TenantRepositoryPort tenantRepository;
  @Mock UserRepositoryPort userRepository;
  @Mock PasswordHasherPort passwordHasher;

  private ChangePasswordUseCase useCase;
  private Tenant tenant;
  private User user;
  private UUID userId;

  @BeforeEach
  void setUp() {
    useCase = new ChangePasswordUseCase(
        signingKeyRepository, accessTokenVerifier,
        tenantRepository, userRepository, passwordHasher);

    userId = UUID.randomUUID();

    tenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME").ownerEmail("owner@acme.com")
        .status(TenantStatus.ACTIVE).build();

    user = User.builder()
        .id(UserId.of(userId))
        .tenantId(tenant.getId())
        .username(Username.of("johndoe"))
        .email(EmailAddress.of("john@acme.com"))
        .passwordHash(PasswordHash.of(CURRENT_HASH))
        .firstName("John").lastName("Doe")
        .status(UserStatus.ACTIVE).build();

    // token stubs comunes — la lista debe ser no vacía para pasar la guarda del use case;
    // el verifier está mockeado independientemente, las claves reales no se usan.
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(mock(SigningKey.class)));
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString()));
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));
  }

  // ─── Escenario 1: cambio exitoso ─────────────────────────────────────────

  @Test
  void execute_shouldChangePassword_whenAllInputsAreValid() {
    // Given
    when(passwordHasher.matches(CURRENT_RAW, CURRENT_HASH)).thenReturn(true);
    when(passwordHasher.matches(NEW_STRONG_PASSWORD, CURRENT_HASH)).thenReturn(false);
    when(passwordHasher.hash(NEW_STRONG_PASSWORD)).thenReturn(NEW_HASH);
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    ChangePasswordResult result = useCase.execute(
        new ChangePasswordCommand(TENANT_SLUG, BEARER_TOKEN, CURRENT_RAW, NEW_STRONG_PASSWORD));

    // Then
    assertThat(result.changed()).isTrue();
  }

  // ─── Escenario 2: contraseña actual incorrecta ────────────────────────────

  @Test
  void execute_shouldThrowIncorrectCurrentPasswordException_whenCurrentPasswordIsWrong() {
    // Given
    when(passwordHasher.matches(any(), eq(CURRENT_HASH))).thenReturn(false);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new ChangePasswordCommand(TENANT_SLUG, BEARER_TOKEN, "WrongPass1!", NEW_STRONG_PASSWORD)))
        .isInstanceOf(IncorrectCurrentPasswordException.class);
  }

  // ─── Escenario 3: nueva contraseña viola la política ─────────────────────

  @Test
  void execute_shouldThrowIllegalArgumentException_whenNewPasswordViolatesPolicy() {
    // Given — current password matches, new password is too short
    when(passwordHasher.matches(CURRENT_RAW, CURRENT_HASH)).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new ChangePasswordCommand(TENANT_SLUG, BEARER_TOKEN, CURRENT_RAW, "short")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("new_password");
  }

  // ─── Escenario 4: nueva contraseña igual a la actual ─────────────────────

  @Test
  void execute_shouldThrowIllegalArgumentException_whenNewPasswordEqualsCurrentPassword() {
    // Given — current password matches AND new password also matches (same password)
    when(passwordHasher.matches(CURRENT_RAW, CURRENT_HASH)).thenReturn(true);
    when(passwordHasher.matches(NEW_STRONG_PASSWORD, CURRENT_HASH)).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new ChangePasswordCommand(TENANT_SLUG, BEARER_TOKEN, CURRENT_RAW, NEW_STRONG_PASSWORD)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("nueva contraseña debe ser diferente");
  }
}
