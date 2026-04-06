package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.platform.request.PlatformLoginRequest;
import io.cmartinezs.keygo.api.platform.request.PlatformTokenRequest;
import io.cmartinezs.keygo.api.platform.response.PlatformTokenData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.platform.command.RotatePlatformRefreshTokenCommand;
import io.cmartinezs.keygo.app.platform.result.IssuePlatformTokensResult;
import io.cmartinezs.keygo.app.platform.usecase.IssuePlatformTokensUseCase;
import io.cmartinezs.keygo.app.platform.usecase.RotatePlatformRefreshTokenUseCase;
import io.cmartinezs.keygo.app.user.command.AuthenticatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.usecase.AuthenticatePlatformUserUseCase;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformAuthControllerTest {

  @Mock AuthenticatePlatformUserUseCase authenticateUseCase;
  @Mock IssuePlatformTokensUseCase issueTokensUseCase;
  @Mock RotatePlatformRefreshTokenUseCase rotateRefreshTokenUseCase;
  @Mock HttpServletRequest httpRequest;

  PlatformAuthController controller;

  private PlatformUser activePlatformUser;
  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    controller = new PlatformAuthController(
        authenticateUseCase, issueTokensUseCase, rotateRefreshTokenUseCase);

    activePlatformUser = PlatformUser.builder()
        .id(UserId.of(userId))
        .username(Username.of("platform_admin"))
        .email(EmailAddress.of("admin@keygo.local"))
        .passwordHash(PasswordHash.of("$2a$10$hashed"))
        .firstName("Admin")
        .lastName("KeyGo")
        .status(UserStatus.ACTIVE)
        .build();
  }

  // ─── Login tests ─────────────────────────────────────────────────────────

  @Test
  void givenValidCredentials_whenLogin_thenReturnsTokens() {
    // Given
    var request = new PlatformLoginRequest("admin@keygo.local", "Admin1234!");
    when(authenticateUseCase.execute(any(AuthenticatePlatformUserCommand.class)))
        .thenReturn(activePlatformUser);
    when(issueTokensUseCase.execute(eq(activePlatformUser), anyString(), any(), any()))
        .thenReturn(new IssuePlatformTokensResult(
            "access.jwt", "refresh.token", "Bearer", 3600L, "key-1"));
    when(httpRequest.getScheme()).thenReturn("http");
    when(httpRequest.getServerName()).thenReturn("localhost");
    when(httpRequest.getServerPort()).thenReturn(8080);
    when(httpRequest.getContextPath()).thenReturn("/keygo-server");
    when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

    // When
    var response = controller.login(request, httpRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess()).isNotNull();
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_LOGIN_SUCCESS.getCode());
    assertThat(response.getBody().getFailure()).isNull();

    PlatformTokenData data = response.getBody().getData();
    assertThat(data.accessToken()).isEqualTo("access.jwt");
    assertThat(data.refreshToken()).isEqualTo("refresh.token");
    assertThat(data.tokenType()).isEqualTo("Bearer");
    assertThat(data.expiresIn()).isEqualTo(3600L);
  }

  @Test
  void givenInvalidPassword_whenLogin_thenThrowsInvalidCredentials() {
    // Given
    var request = new PlatformLoginRequest("admin@keygo.local", "wrong-password");
    when(authenticateUseCase.execute(any(AuthenticatePlatformUserCommand.class)))
        .thenThrow(new InvalidCredentialsException());

    // When / Then
    assertThatThrownBy(() -> controller.login(request, httpRequest))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void givenUnknownEmail_whenLogin_thenThrowsUserNotFound() {
    // Given
    var request = new PlatformLoginRequest("unknown@keygo.local", "password");
    when(authenticateUseCase.execute(any(AuthenticatePlatformUserCommand.class)))
        .thenThrow(new UserNotFoundException("email", "unknown@keygo.local"));

    // When / Then
    assertThatThrownBy(() -> controller.login(request, httpRequest))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void givenSuspendedUser_whenLogin_thenThrowsUserSuspended() {
    // Given
    var request = new PlatformLoginRequest("suspended@keygo.local", "Admin1234!");
    when(authenticateUseCase.execute(any(AuthenticatePlatformUserCommand.class)))
        .thenThrow(new UserSuspendedException("suspended_admin"));

    // When / Then
    assertThatThrownBy(() -> controller.login(request, httpRequest))
        .isInstanceOf(UserSuspendedException.class);
  }

  // ─── Token rotation tests ────────────────────────────────────────────────

  @Test
  void givenValidRefreshToken_whenTokenRotation_thenReturnsNewTokens() {
    // Given
    var request = new PlatformTokenRequest("refresh_token", "raw-refresh-token");
    when(rotateRefreshTokenUseCase.execute(any(RotatePlatformRefreshTokenCommand.class)))
        .thenReturn(new IssuePlatformTokensResult(
            "new-access.jwt", "new-refresh.token", "Bearer", 3600L, "key-2"));

    // When
    var response = controller.token(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_TOKEN_ROTATED.getCode());

    PlatformTokenData data = response.getBody().getData();
    assertThat(data.accessToken()).isEqualTo("new-access.jwt");
    assertThat(data.refreshToken()).isEqualTo("new-refresh.token");
  }

  @Test
  void givenUnsupportedGrantType_whenToken_thenReturnsBadRequest() {
    // Given
    var request = new PlatformTokenRequest("authorization_code", "some-code");

    // When
    var response = controller.token(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.INVALID_INPUT.getCode());
  }
}
