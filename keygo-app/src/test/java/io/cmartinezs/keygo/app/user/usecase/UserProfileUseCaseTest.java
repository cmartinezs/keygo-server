package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.GetUserProfileCommand;
import io.cmartinezs.keygo.app.user.command.UpdateUserProfileCommand;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
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
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
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
 * Tests unitarios para GetUserProfileUseCase y UpdateUserProfileUseCase.
 */
@ExtendWith(MockitoExtension.class)
class UserProfileUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String BEARER_TOKEN = "valid.jwt.token";

  @Mock SigningKeyRepositoryPort signingKeyRepository;
  @Mock AccessTokenVerifierPort accessTokenVerifier;
  @Mock UserRepositoryPort userRepository;
  @Mock TenantRepositoryPort tenantRepository;

  private Tenant tenant;
  private User user;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    tenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME").ownerEmail("o@acme.com")
        .status(TenantStatus.ACTIVE).build();

    user = User.builder()
        .id(new UserId(userId))
        .tenantId(tenant.getId())
        .username(Username.of("johndoe"))
        .email(EmailAddress.of("john@acme.com"))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .firstName("John").lastName("Doe")
        .status(UserStatus.ACTIVE)
        .locale("en-US")
        .build();
  }

  // ─── GetUserProfileUseCase ────────────────────────────────────────────────

  @Test
  void getProfile_returnsFullProfile() {
    // Given
    SigningKey key = mock(SigningKey.class);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(key));
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString()));
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));

    GetUserProfileUseCase uc = new GetUserProfileUseCase(
        signingKeyRepository, accessTokenVerifier, userRepository, tenantRepository);

    // When
    UserProfileResult result = uc.execute(new GetUserProfileCommand(TENANT_SLUG, BEARER_TOKEN));

    // Then
    assertThat(result.id()).isEqualTo(userId.toString());
    assertThat(result.email()).isEqualTo("john@acme.com");
    assertThat(result.username()).isEqualTo("johndoe");
    assertThat(result.firstName()).isEqualTo("John");
    assertThat(result.lastName()).isEqualTo("Doe");
    assertThat(result.status()).isEqualTo("ACTIVE");
    assertThat(result.locale()).isEqualTo("en-US");
  }

  @Test
  void getProfile_throwsWhenNoPublicKeys() {
    // Given
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of());

    GetUserProfileUseCase uc = new GetUserProfileUseCase(
        signingKeyRepository, accessTokenVerifier, userRepository, tenantRepository);
    var command = new GetUserProfileCommand(TENANT_SLUG, BEARER_TOKEN);

    // When / Then
    assertThatThrownBy(() -> uc.execute(command))
        .isInstanceOf(InvalidRefreshTokenException.class)
        .hasMessageContaining("No public signing keys");
  }

  @Test
  void getProfile_throwsWhenTokenMissingSubClaim() {
    // Given
    SigningKey key = mock(SigningKey.class);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(key));
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("email", "john@acme.com")); // sin 'sub'

    GetUserProfileUseCase uc = new GetUserProfileUseCase(
        signingKeyRepository, accessTokenVerifier, userRepository, tenantRepository);
    var command = new GetUserProfileCommand(TENANT_SLUG, BEARER_TOKEN);

    // When / Then
    assertThatThrownBy(() -> uc.execute(command))
        .isInstanceOf(InvalidRefreshTokenException.class)
        .hasMessageContaining("missing 'sub'");
  }

  @Test
  void getProfile_throwsWhenUserNotFound() {
    // Given
    SigningKey key = mock(SigningKey.class);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(key));
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString()));
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.empty());

    GetUserProfileUseCase uc = new GetUserProfileUseCase(
        signingKeyRepository, accessTokenVerifier, userRepository, tenantRepository);
    var command = new GetUserProfileCommand(TENANT_SLUG, BEARER_TOKEN);

    // When / Then
    assertThatThrownBy(() -> uc.execute(command))
        .isInstanceOf(UserNotFoundException.class);
  }

  // ─── UpdateUserProfileUseCase ─────────────────────────────────────────────

  @Test
  void updateProfile_updatesFieldsAndReturnsResult() {
    // Given
    SigningKey key = mock(SigningKey.class);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(key));
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString()));
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    UpdateUserProfileUseCase uc = new UpdateUserProfileUseCase(
        signingKeyRepository, accessTokenVerifier, userRepository, tenantRepository);

    // When
    UserProfileResult result = uc.execute(new UpdateUserProfileCommand(
        TENANT_SLUG, BEARER_TOKEN,
        "Juan", "García",
        "+521234567890", "es-MX", "America/Mexico_City",
        "https://cdn.example.com/pic.jpg", "1990-01-15", "https://juangarcia.dev"));

    // Then
    assertThat(result.firstName()).isEqualTo("Juan");
    assertThat(result.lastName()).isEqualTo("García");
    assertThat(result.phoneNumber()).isEqualTo("+521234567890");
    assertThat(result.locale()).isEqualTo("es-MX");
    assertThat(result.zoneinfo()).isEqualTo("America/Mexico_City");
    assertThat(result.profilePictureUrl()).isEqualTo("https://cdn.example.com/pic.jpg");
    assertThat(result.birthdate()).isEqualTo("1990-01-15");
    assertThat(result.website()).isEqualTo("https://juangarcia.dev");
  }

  @Test
  void updateProfile_nullFieldsDoNotOverwriteExisting() {
    // Given — el usuario ya tiene locale="en-US"
    SigningKey key = mock(SigningKey.class);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(key));
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString()));
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    UpdateUserProfileUseCase uc = new UpdateUserProfileUseCase(
        signingKeyRepository, accessTokenVerifier, userRepository, tenantRepository);

    // When — solo se envía firstName, el resto son null
    UserProfileResult result = uc.execute(new UpdateUserProfileCommand(
        TENANT_SLUG, BEARER_TOKEN,
        "Carlos", null, null, null, null, null, null, null));

    // Then — firstName actualizado, locale sin cambio (era "en-US")
    assertThat(result.firstName()).isEqualTo("Carlos");
    assertThat(result.locale()).isEqualTo("en-US");
  }

  @Test
  void updateProfile_throwsWhenTenantNotFound() {
    // Given
    SigningKey key = mock(SigningKey.class);
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(key));
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString()));
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.empty());

    UpdateUserProfileUseCase uc = new UpdateUserProfileUseCase(
        signingKeyRepository, accessTokenVerifier, userRepository, tenantRepository);
    var command = new UpdateUserProfileCommand(
        TENANT_SLUG, BEARER_TOKEN, null, null, null, null, null, null, null, null);

    // When / Then
    assertThatThrownBy(() -> uc.execute(command))
        .isInstanceOf(InvalidRefreshTokenException.class)
        .hasMessageContaining("Tenant not found");
  }
}

