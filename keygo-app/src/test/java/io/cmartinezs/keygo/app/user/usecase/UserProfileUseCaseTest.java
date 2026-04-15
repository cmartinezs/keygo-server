package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.GetUserProfileCommand;
import io.cmartinezs.keygo.app.user.command.UpdateUserProfileCommand;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para GetUserProfileUseCase y UpdateUserProfileUseCase.
 *
 * <p>La verificación JWT es responsabilidad del filtro; los use cases reciben
 * el userId (claim {@code sub}) ya extraído del SecurityContext por el controller.
 */
@ExtendWith(MockitoExtension.class)
class UserProfileUseCaseTest {

  private static final String TENANT_SLUG = "acme";

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
        .name("ACME")
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
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));

    GetUserProfileUseCase uc = new GetUserProfileUseCase(userRepository, tenantRepository);

    UserProfileResult result = uc.execute(new GetUserProfileCommand(TENANT_SLUG, userId.toString()));

    assertThat(result.id()).isEqualTo(userId.toString());
    assertThat(result.email()).isEqualTo("john@acme.com");
    assertThat(result.username()).isEqualTo("johndoe");
    assertThat(result.firstName()).isEqualTo("John");
    assertThat(result.lastName()).isEqualTo("Doe");
    assertThat(result.status()).isEqualTo("ACTIVE");
    assertThat(result.locale()).isEqualTo("en-US");
  }

  @Test
  void getProfile_throwsWhenUserIdIsInvalidUuid() {
    GetUserProfileUseCase uc = new GetUserProfileUseCase(userRepository, tenantRepository);

    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));

    assertThatThrownBy(() -> uc.execute(new GetUserProfileCommand(TENANT_SLUG, "not-a-uuid")))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void getProfile_throwsWhenTenantNotFound() {
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.empty());

    GetUserProfileUseCase uc = new GetUserProfileUseCase(userRepository, tenantRepository);

    assertThatThrownBy(() -> uc.execute(new GetUserProfileCommand(TENANT_SLUG, userId.toString())))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void getProfile_throwsWhenUserNotFound() {
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.empty());

    GetUserProfileUseCase uc = new GetUserProfileUseCase(userRepository, tenantRepository);

    assertThatThrownBy(() -> uc.execute(new GetUserProfileCommand(TENANT_SLUG, userId.toString())))
        .isInstanceOf(UserNotFoundException.class);
  }

  // ─── UpdateUserProfileUseCase ─────────────────────────────────────────────

  @Test
  void updateProfile_updatesFieldsAndReturnsResult() {
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    UpdateUserProfileUseCase uc = new UpdateUserProfileUseCase(userRepository, tenantRepository);

    UserProfileResult result = uc.execute(new UpdateUserProfileCommand(
        TENANT_SLUG, userId.toString(),
        "Juan", "García",
        "+521234567890", "es-MX", "America/Mexico_City",
        "https://cdn.example.com/pic.jpg", "1990-01-15", "https://juangarcia.dev"));

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
    // El usuario ya tiene locale="en-US"
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    UpdateUserProfileUseCase uc = new UpdateUserProfileUseCase(userRepository, tenantRepository);

    // Solo se envía firstName; el resto son null → no deben cambiar
    UserProfileResult result = uc.execute(new UpdateUserProfileCommand(
        TENANT_SLUG, userId.toString(),
        "Carlos", null, null, null, null, null, null, null));

    assertThat(result.firstName()).isEqualTo("Carlos");
    assertThat(result.locale()).isEqualTo("en-US");
  }

  @Test
  void updateProfile_throwsWhenTenantNotFound() {
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.empty());

    UpdateUserProfileUseCase uc = new UpdateUserProfileUseCase(userRepository, tenantRepository);
    var command = new UpdateUserProfileCommand(
        TENANT_SLUG, userId.toString(), null, null, null, null, null, null, null, null);

    assertThatThrownBy(() -> uc.execute(command))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void updateProfile_throwsWhenUserIdIsInvalidUuid() {
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));

    UpdateUserProfileUseCase uc = new UpdateUserProfileUseCase(userRepository, tenantRepository);
    var command = new UpdateUserProfileCommand(
        TENANT_SLUG, "not-a-uuid", null, null, null, null, null, null, null, null);

    assertThatThrownBy(() -> uc.execute(command))
        .isInstanceOf(UserNotFoundException.class);
  }
}
