package io.cmartinezs.keygo.api.platform.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.api.platform.request.AssignPlatformRoleRequest;
import io.cmartinezs.keygo.api.platform.request.CreatePlatformUserRequest;
import io.cmartinezs.keygo.api.platform.response.PlatformUserData;
import io.cmartinezs.keygo.api.platform.response.PlatformUserRoleData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.response.PagedData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.shared.exception.InvalidPaginationParamException;
import io.cmartinezs.keygo.app.membership.command.AssignPlatformRoleCommand;
import io.cmartinezs.keygo.app.membership.result.PlatformUserRoleResult;
import io.cmartinezs.keygo.app.membership.usecase.AssignPlatformRoleUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListPlatformUserRolesUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokePlatformRoleUseCase;
import io.cmartinezs.keygo.app.user.command.CreatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.usecase.ActivatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.CreatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetPlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListPlatformUsersUseCase;
import io.cmartinezs.keygo.app.user.usecase.SuspendPlatformUserUseCase;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.List;
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
  @Mock private ListPlatformUsersUseCase listPlatformUsersUseCase;
  @Mock private GetPlatformUserUseCase getPlatformUserUseCase;
  @Mock private SuspendPlatformUserUseCase suspendPlatformUserUseCase;
  @Mock private ActivatePlatformUserUseCase activatePlatformUserUseCase;
  @Mock private ListPlatformUserRolesUseCase listPlatformUserRolesUseCase;
  @Mock private AssignPlatformRoleUseCase assignPlatformRoleUseCase;
  @Mock private RevokePlatformRoleUseCase revokePlatformRoleUseCase;

  private PlatformUserController controller;

  private static final UUID USER_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    controller = new PlatformUserController(
        createPlatformUserUseCase,
        listPlatformUsersUseCase,
        getPlatformUserUseCase,
        suspendPlatformUserUseCase,
        activatePlatformUserUseCase,
        listPlatformUserRolesUseCase,
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
  @DisplayName("GET /platform/users should return 200 with paginated user list")
  void shouldListPlatformUsersAndReturn200() {
    PlatformUser first = buildUser(USER_ID, UserStatus.ACTIVE);
    PlatformUser second = buildUser(UUID.randomUUID(), UserStatus.SUSPENDED);
    when(listPlatformUsersUseCase.execute(any()))
        .thenReturn(PagedResult.of(java.util.List.of(first, second), 0, 20, 2));

    ResponseEntity<BaseResponse<PagedData<PlatformUserData>>> response =
        controller.listPlatformUsers(null, null, null, 0, 20, null, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_USER_LIST_RETRIEVED.getCode());
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getContent()).hasSize(2);
    assertThat(response.getBody().getData().getTotalElements()).isEqualTo(2L);
    assertThat(response.getBody().getData().isLast()).isTrue();
  }

  @Test
  @DisplayName("GET /platform/users should pass filter values to use case")
  void shouldPassListFilterToUseCase() {
    when(listPlatformUsersUseCase.execute(any()))
        .thenReturn(PagedResult.of(java.util.List.of(), 1, 10, 0));

    controller.listPlatformUsers(UserStatus.ACTIVE, "admin", "keygo.io", 1, 10, "email", "DESC");

    verify(listPlatformUsersUseCase)
        .execute(
            argThat(
                filter ->
                    filter.hasStatus()
                        && filter.getStatus() == UserStatus.ACTIVE
                        && "admin".equals(filter.getUsernameLike())
                        && "keygo.io".equals(filter.getEmailLike())
                        && filter.getPage() == 1
                        && filter.getSize() == 10
                        && "email".equals(filter.getSortBy())
                        && "DESC".equals(filter.getSortOrder())));
  }

  @Test
  @DisplayName("GET /platform/users should propagate invalid pagination params")
  void shouldPropagateInvalidPaginationParams() {
    assertThatThrownBy(() -> controller.listPlatformUsers(null, null, null, -1, 20, null, null))
        .isInstanceOf(InvalidPaginationParamException.class)
        .hasMessageContaining("Pagination parameter 'page' is invalid");
  }

  @Test
  @DisplayName("GET /platform/users/{userId}/platform-roles should return 200 with assigned roles")
  void shouldListPlatformUserRolesAndReturn200() {
    when(listPlatformUserRolesUseCase.execute(USER_ID))
        .thenReturn(
            java.util.List.of(
                new PlatformUserRoleResult(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "keygo_admin",
                    "KeyGo Admin",
                    "Full administrative access",
                    "CONTRACTOR",
                    UUID.fromString("33000000-0000-0000-0000-000000000001"),
                    UUID.fromString("44000000-0000-0000-0000-000000000001"),
                    new io.cmartinezs.keygo.app.membership.result.PlatformRoleContractorResult(
                        UUID.fromString("33000000-0000-0000-0000-000000000001"),
                        "Acme SpA",
                        "billing@acme.cl"),
                    java.time.Instant.parse("2026-04-13T08:00:00Z"))));

    ResponseEntity<BaseResponse<java.util.List<PlatformUserRoleData>>> response =
        controller.listPlatformUserRoles(USER_ID);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_ROLE_LIST_RETRIEVED.getCode());
    assertThat(response.getBody().getData()).hasSize(1);
    assertThat(response.getBody().getData().getFirst().getAssignmentId()).isNotBlank();
    assertThat(response.getBody().getData().getFirst().getRoleCode()).isEqualTo("keygo_admin");
    assertThat(response.getBody().getData().getFirst().getRoleName()).isEqualTo("KeyGo Admin");
    assertThat(response.getBody().getData().getFirst().getScopeType()).isEqualTo("CONTRACTOR");
    assertThat(response.getBody().getData().getFirst().getContractor()).isNotNull();
    assertThat(response.getBody().getData().getFirst().getContractor().getDisplayName())
        .isEqualTo("Acme SpA");
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
    AssignPlatformRoleRequest request = new AssignPlatformRoleRequest(List.of("keygo_admin"));
    when(assignPlatformRoleUseCase.execute(any(AssignPlatformRoleCommand.class)))
        .thenReturn(List.of());

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
