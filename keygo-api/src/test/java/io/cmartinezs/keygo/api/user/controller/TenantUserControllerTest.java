package io.cmartinezs.keygo.api.user.controller;

import io.cmartinezs.keygo.api.user.request.CreateUserRequest;
import io.cmartinezs.keygo.api.user.request.ResetPasswordRequest;
import io.cmartinezs.keygo.api.user.request.UpdateUserRequest;
import io.cmartinezs.keygo.api.user.request.ValidateCredentialsRequest;
import io.cmartinezs.keygo.api.user.response.UserData;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.user.filter.UserFilter;
import io.cmartinezs.keygo.app.user.result.UserStatusActionResult;
import io.cmartinezs.keygo.app.user.usecase.ActivateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.CreateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListUsersUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResetUserPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListAdminUserSessionsUseCase;
import io.cmartinezs.keygo.app.user.usecase.SuspendUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ValidateUserCredentialsUseCase;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantUserControllerTest {

  private static final String TENANT_SLUG = "acme";

  @Mock CreateUserUseCase createUserUseCase;
  @Mock ListUsersUseCase listUsersUseCase;
  @Mock GetUserUseCase getUserUseCase;
  @Mock UpdateUserUseCase updateUserUseCase;
  @Mock ResetUserPasswordUseCase resetUserPasswordUseCase;
  @Mock ValidateUserCredentialsUseCase validateUserCredentialsUseCase;
  @Mock SuspendUserUseCase suspendUserUseCase;
  @Mock ActivateUserUseCase activateUserUseCase;
  @Mock ListAdminUserSessionsUseCase listAdminUserSessionsUseCase;

  private TenantUserController controller;
  private User sampleUser;

  @BeforeEach
  void setUp() {
    controller = new TenantUserController(
        createUserUseCase, listUsersUseCase, getUserUseCase,
        updateUserUseCase, resetUserPasswordUseCase, validateUserCredentialsUseCase,
        suspendUserUseCase, activateUserUseCase, listAdminUserSessionsUseCase);

    sampleUser = User.builder()
        .id(UserId.of(UUID.randomUUID()))
        .tenantId(TenantId.of(UUID.randomUUID()))
        .username(Username.of("johndoe"))
        .email(EmailAddress.of("john@acme.com"))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .firstName("John").lastName("Doe")
        .status(UserStatus.ACTIVE)
        .build();
  }

  @Test
  void createUserReturns201() {
    // Given
    when(createUserUseCase.execute(any())).thenReturn(sampleUser);
    CreateUserRequest request = new CreateUserRequest("johndoe", "john@acme.com", "secret123", "John", "Doe");

    // When
    ResponseEntity<?> response = controller.createUser(TENANT_SLUG, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void listUsersReturns200() {
    // Given
    PagedResult<User> pagedResult = PagedResult.of(List.of(sampleUser), 0, 20, 1);
    when(listUsersUseCase.execute(eq(TENANT_SLUG), any(UserFilter.class))).thenReturn(pagedResult);

    // When
    var response = controller.listUsers(TENANT_SLUG, null, null, null, null, 0, 20, null, null);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().getContent()).hasSize(1);
  }

  @Test
  void getUserReturns200() {
    // Given
    when(getUserUseCase.execute(any(), any())).thenReturn(sampleUser);

    // When
    var response = controller.getUser(TENANT_SLUG, sampleUser.getId().toString());

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    UserData data = response.getBody().getData();
    assertThat(data.getUsername()).isEqualTo("johndoe");
    assertThat(data.getEmail()).isEqualTo("john@acme.com");
  }

  @Test
  void updateUserReturns200() {
    // Given
    when(updateUserUseCase.execute(any())).thenReturn(sampleUser);

    // When
    var response = controller.updateUser(TENANT_SLUG, sampleUser.getId().toString(),
        new UpdateUserRequest("Jane", "Smith", null, null, null, null, null, null));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void resetPasswordReturns200() {
    // Given
    when(resetUserPasswordUseCase.execute(any())).thenReturn(sampleUser);

    // When
    var response = controller.resetPassword(TENANT_SLUG, sampleUser.getId().toString(), new ResetPasswordRequest("newpass12"));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void validateCredentialsReturns200() {
    // Given
    when(validateUserCredentialsUseCase.execute(any(), any(), any())).thenReturn(sampleUser);

    // When
    var response = controller.validateCredentials(TENANT_SLUG, new ValidateCredentialsRequest("john@acme.com", "secret123"));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().getStatus()).isEqualTo("ACTIVE");
  }

  @Test
  void suspendUserReturns200WithStatusTransition() {
    // Given
    var result = new UserStatusActionResult(
        sampleUser.getId().value(), UserStatus.ACTIVE, UserStatus.SUSPENDED, false);
    when(suspendUserUseCase.execute(any(), any())).thenReturn(result);

    // When
    var response = controller.suspendUser(TENANT_SLUG, sampleUser.getId().toString());

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var data = response.getBody().getData();
    assertThat(data.previousStatus()).isEqualTo("ACTIVE");
    assertThat(data.currentStatus()).isEqualTo("SUSPENDED");
    assertThat(data.alreadySuspended()).isFalse();
    assertThat(data.alreadyActive()).isFalse();
  }

  @Test
  void suspendUserReturns200WhenAlreadySuspended() {
    // Given
    var result = new UserStatusActionResult(
        sampleUser.getId().value(), UserStatus.SUSPENDED, UserStatus.SUSPENDED, true);
    when(suspendUserUseCase.execute(any(), any())).thenReturn(result);

    // When
    var response = controller.suspendUser(TENANT_SLUG, sampleUser.getId().toString());

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var data = response.getBody().getData();
    assertThat(data.alreadySuspended()).isTrue();
    assertThat(data.alreadyActive()).isFalse();
  }

  @Test
  void activateUserReturns200WithStatusTransition() {
    // Given
    var result = new UserStatusActionResult(
        sampleUser.getId().value(), UserStatus.SUSPENDED, UserStatus.ACTIVE, false);
    when(activateUserUseCase.execute(any(), any())).thenReturn(result);

    // When
    var response = controller.activateUser(TENANT_SLUG, sampleUser.getId().toString());

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var data = response.getBody().getData();
    assertThat(data.previousStatus()).isEqualTo("SUSPENDED");
    assertThat(data.currentStatus()).isEqualTo("ACTIVE");
    assertThat(data.alreadyActive()).isFalse();
    assertThat(data.alreadySuspended()).isFalse();
  }

  @Test
  void activateUserReturns200WhenAlreadyActive() {
    // Given
    var result = new UserStatusActionResult(
        sampleUser.getId().value(), UserStatus.ACTIVE, UserStatus.ACTIVE, true);
    when(activateUserUseCase.execute(any(), any())).thenReturn(result);

    // When
    var response = controller.activateUser(TENANT_SLUG, sampleUser.getId().toString());

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    var data = response.getBody().getData();
    assertThat(data.alreadyActive()).isTrue();
    assertThat(data.alreadySuspended()).isFalse();
  }
}

