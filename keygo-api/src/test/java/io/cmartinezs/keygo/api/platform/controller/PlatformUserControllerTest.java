package io.cmartinezs.keygo.api.platform.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.api.platform.request.AssignPlatformRoleRequest;
import io.cmartinezs.keygo.api.platform.request.CreatePlatformUserRequest;
import io.cmartinezs.keygo.api.platform.response.PlatformUserData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.membership.command.AssignPlatformRoleCommand;
import io.cmartinezs.keygo.app.membership.usecase.AssignPlatformRoleUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokePlatformRoleUseCase;
import io.cmartinezs.keygo.app.user.command.CreatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.usecase.ActivatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.CreatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetPlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.SuspendPlatformUserUseCase;
import io.cmartinezs.keygo.domain.membership.model.PlatformRoleId;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRole;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRoleId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformUserController")
class PlatformUserControllerTest {

  @Mock private CreatePlatformUserUseCase createPlatformUserUseCase;
  @Mock private GetPlatformUserUseCase getPlatformUserUseCase;
  @Mock private SuspendPlatformUserUseCase suspendPlatformUserUseCase;
  @Mock private ActivatePlatformUserUseCase activatePlatformUserUseCase;
  @Mock private AssignPlatformRoleUseCase assignPlatformRoleUseCase;
  @Mock private RevokePlatformRoleUseCase revokePlatformRoleUseCase;

  private PlatformUserController controller;

  private static final UUID USER_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    controller = new PlatformUserController(
        createPlatformUserUseCase,
        getPlatformUserUseCase,
        suspendPlatformUserUseCase,
        activatePlatformUserUseCase,
        assignPlatformRoleUseCase,
        revokePlatformRoleUseCase);
  }

  private PlatformUser buildUser(UUID id, UserStatus status) {
    return PlatformUser.builder()
        .id(UserId.of(id))
        .username(Username.of("testuser"))
        .email(EmailAddress.of("test@example.com"))
        .passwordHash(PasswordHash.of("$2a$10$hashedpassword"))
        .firstName("Test")
        .lastName("User")
        .status(status)
        .build();
  }

  @Test
  @DisplayName("POST /platform/users should return 201 with created user data")
  void shouldCreatePlatformUserAndReturn201() {
    // Given
    CreatePlatformUserRequest request =
        new CreatePlatformUserRequest("test@example.com", "Password1!", "Test", "User");
    PlatformUser created = buildUser(USER_ID, UserStatus.ACTIVE);
    when(createPlatformUserUseCase.execute(any(CreatePlatformUserCommand.class)))
        .thenReturn(created);

    // When
    ResponseEntity<BaseResponse<PlatformUserData>> response =
        controller.createPlatformUser(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getEmail()).isEqualTo("test@example.com");
    assertThat(response.getBody().getData().getStatus()).isEqualTo("ACTIVE");
    assertThat(response.getBody().getSuccess()).isNotNull();
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_USER_CREATED.getCode());
  }

  @Test
  @DisplayName("GET /platform/users/{userId} should return 200 with user data")
  void shouldGetPlatformUserAndReturn200() {
    // Given
    PlatformUser user = buildUser(USER_ID, UserStatus.ACTIVE);
    when(getPlatformUserUseCase.execute(UserId.of(USER_ID))).thenReturn(user);

    // When
    ResponseEntity<BaseResponse<PlatformUserData>> response =
        controller.getPlatformUser(USER_ID);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getUsername()).isEqualTo("testuser");
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_USER_RETRIEVED.getCode());
  }

  @Test
  @DisplayName("PUT /platform/users/{userId}/suspend should return 200 with suspended user")
  void shouldSuspendPlatformUserAndReturn200() {
    // Given
    PlatformUser suspended = buildUser(USER_ID, UserStatus.SUSPENDED);
    when(suspendPlatformUserUseCase.execute(UserId.of(USER_ID))).thenReturn(suspended);

    // When
    ResponseEntity<BaseResponse<PlatformUserData>> response =
        controller.suspendPlatformUser(USER_ID);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().getStatus()).isEqualTo("SUSPENDED");
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_USER_SUSPENDED.getCode());
  }

  @Test
  @DisplayName("PUT /platform/users/{userId}/activate should return 200 with activated user")
  void shouldActivatePlatformUserAndReturn200() {
    // Given
    PlatformUser activated = buildUser(USER_ID, UserStatus.ACTIVE);
    when(activatePlatformUserUseCase.execute(UserId.of(USER_ID))).thenReturn(activated);

    // When
    ResponseEntity<BaseResponse<PlatformUserData>> response =
        controller.activatePlatformUser(USER_ID);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().getStatus()).isEqualTo("ACTIVE");
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_USER_ACTIVATED.getCode());
  }

  @Test
  @DisplayName("POST /platform/users/{userId}/platform-roles should return 200")
  void shouldAssignPlatformRoleAndReturn200() {
    // Given
    AssignPlatformRoleRequest request = new AssignPlatformRoleRequest("keygo_admin");
    PlatformUserRole role = PlatformUserRole.builder()
        .id(PlatformUserRoleId.of(UUID.randomUUID()))
        .userId(UserId.of(USER_ID))
        .platformRoleId(PlatformRoleId.of(UUID.randomUUID()))
        .build();
    when(assignPlatformRoleUseCase.execute(any(AssignPlatformRoleCommand.class)))
        .thenReturn(role);

    // When
    ResponseEntity<BaseResponse<Void>> response =
        controller.assignPlatformRole(USER_ID, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_ROLE_ASSIGNED.getCode());
  }

  @Test
  @DisplayName("DELETE /platform/users/{userId}/platform-roles/{roleCode} should return 200")
  void shouldRevokePlatformRoleAndReturn200() {
    // When
    ResponseEntity<BaseResponse<Void>> response =
        controller.revokePlatformRole(USER_ID, "keygo_admin");

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_ROLE_REVOKED.getCode());
    verify(revokePlatformRoleUseCase).execute(USER_ID, "keygo_admin");
  }
}
