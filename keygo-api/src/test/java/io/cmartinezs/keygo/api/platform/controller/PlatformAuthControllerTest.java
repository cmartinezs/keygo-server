package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.platform.request.PlatformLoginRequest;
import io.cmartinezs.keygo.api.platform.request.PlatformTokenRequest;
import io.cmartinezs.keygo.api.platform.response.PlatformAuthorizationData;
import io.cmartinezs.keygo.api.platform.response.PlatformLoginData;
import io.cmartinezs.keygo.api.platform.response.PlatformTokenData;
import io.cmartinezs.keygo.api.platform.session.PlatformAuthCodeState;
import io.cmartinezs.keygo.api.platform.session.PlatformAuthorizationSessionState;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.platform.command.RotatePlatformRefreshTokenCommand;
import io.cmartinezs.keygo.app.platform.port.PlatformConfigPort;
import io.cmartinezs.keygo.app.platform.result.IssuePlatformTokensResult;
import io.cmartinezs.keygo.app.platform.usecase.IssuePlatformTokensUseCase;
import io.cmartinezs.keygo.app.platform.usecase.RotatePlatformRefreshTokenUseCase;
import io.cmartinezs.keygo.app.user.command.AuthenticatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.usecase.AuthenticatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetPlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.SendPlatformPasswordResetCodeUseCase;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserPasswordResetRequiredException;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
  @Mock GetPlatformUserUseCase getPlatformUserUseCase;
  @Mock SendPlatformPasswordResetCodeUseCase sendPlatformPasswordResetCodeUseCase;
  @Mock HttpServletRequest httpRequest;
  @Mock HttpSession httpSession;

  PlatformAuthController controller;

  private PlatformUser activePlatformUser;
  private final UUID userId = UUID.randomUUID();
  private static final List<String> ALLOWED_URIS = List.of(
      "http://localhost:5173/callback", "http://localhost:5173/platform/callback");
  private static final String APP_NAME = "KeyGo Platform";

  private final PlatformConfigPort platformConfig = new PlatformConfigPort() {
    @Override public List<String> getAllowedRedirectUris() { return ALLOWED_URIS; }
    @Override public String getApplicationName() { return APP_NAME; }
  };

  @BeforeEach
  void setUp() {
    controller = new PlatformAuthController(
        authenticateUseCase, issueTokensUseCase, rotateRefreshTokenUseCase,
        getPlatformUserUseCase, sendPlatformPasswordResetCodeUseCase, platformConfig);

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

  // ─── Authorize tests ────────────────────────────────────────────────────────

  @Nested
  class AuthorizeTests {

    @Test
    void givenValidParams_whenAuthorize_thenStoresSessionStateAndReturnsData() {
      // Given
      when(httpRequest.getSession(true)).thenReturn(httpSession);

      // When
      var response = controller.authorize(
          "http://localhost:5173/callback", "openid profile platform",
          "challenge123", "S256", httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getSuccess().getCode())
          .isEqualTo(ResponseCode.PLATFORM_AUTHORIZATION_INITIATED.getCode());

      PlatformAuthorizationData data = response.getBody().getData();
      assertThat(data.applicationName()).isEqualTo(APP_NAME);
      assertThat(data.redirectUri()).isEqualTo("http://localhost:5173/callback");

      verify(httpSession).setAttribute(
          eq(PlatformAuthController.SESSION_ATTR_AUTH_STATE),
          any(PlatformAuthorizationSessionState.class));
    }

    @Test
    void givenInvalidRedirectUri_whenAuthorize_thenReturnsBadRequest() {
      // When
      var response = controller.authorize(
          "http://evil.com/callback", "openid", "challenge", "S256", httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody().getFailure()).isNotNull();
      assertThat(response.getBody().getFailure().getCode())
          .isEqualTo(ResponseCode.INVALID_INPUT.getCode());
    }

    @Test
    void givenUnsupportedMethod_whenAuthorize_thenReturnsBadRequest() {
      // When
      var response = controller.authorize(
          "http://localhost:5173/callback", "openid", "challenge", "plain", httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody().getFailure().getCode())
          .isEqualTo(ResponseCode.UNSUPPORTED_PKCE_METHOD.getCode());
    }

    @Test
    void givenBlankCodeChallenge_whenAuthorize_thenReturnsBadRequest() {
      // When
      var response = controller.authorize(
          "http://localhost:5173/callback", "openid", "  ", "S256", httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody().getFailure().getCode())
          .isEqualTo(ResponseCode.REQUIRED_FIELD_MISSING.getCode());
    }
  }

  // ─── Login (PKCE flow) tests ────────────────────────────────────────────────

  @Nested
  class LoginTests {

    @Test
    void givenValidSession_whenLogin_thenReturnsAuthorizationCode() {
      // Given
      var authState = new PlatformAuthorizationSessionState(
          "http://localhost:5173/callback", "openid", "challenge", "S256");
      when(httpRequest.getSession(false)).thenReturn(httpSession);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_STATE))
          .thenReturn(authState);
      when(authenticateUseCase.execute(any(AuthenticatePlatformUserCommand.class)))
          .thenReturn(activePlatformUser);

      var request = new PlatformLoginRequest("admin@keygo.local", "Admin1234!");

      // When
      var response = controller.login(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getSuccess().getCode())
          .isEqualTo(ResponseCode.PLATFORM_LOGIN_SUCCESS.getCode());

      PlatformLoginData data = response.getBody().getData();
      assertThat(data.code()).isNotNull().isNotBlank();
      assertThat(data.redirectUri()).isEqualTo("http://localhost:5173/callback");
      assertThat(data.message()).isNotBlank();

      verify(httpSession).setAttribute(
          eq(PlatformAuthController.SESSION_ATTR_AUTH_CODE),
          any(PlatformAuthCodeState.class));
    }

    @Test
    void givenNoSession_whenLogin_thenReturnsBadRequest() {
      // Given
      when(httpRequest.getSession(false)).thenReturn(null);
      var request = new PlatformLoginRequest("admin@keygo.local", "Admin1234!");

      // When
      var response = controller.login(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody().getFailure()).isNotNull();
    }

    @Test
    void givenNoAuthState_whenLogin_thenReturnsBadRequest() {
      // Given
      when(httpRequest.getSession(false)).thenReturn(httpSession);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_STATE))
          .thenReturn(null);
      var request = new PlatformLoginRequest("admin@keygo.local", "Admin1234!");

      // When
      var response = controller.login(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody().getFailure()).isNotNull();
    }

    @Test
    void givenInvalidCredentials_whenLogin_thenThrowsException() {
      // Given
      var authState = new PlatformAuthorizationSessionState(
          "http://localhost:5173/callback", "openid", "challenge", "S256");
      when(httpRequest.getSession(false)).thenReturn(httpSession);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_STATE))
          .thenReturn(authState);
      when(authenticateUseCase.execute(any(AuthenticatePlatformUserCommand.class)))
          .thenThrow(new InvalidCredentialsException());
      var request = new PlatformLoginRequest("admin@keygo.local", "wrong");

      // When / Then
      assertThatThrownBy(() -> controller.login(request, httpRequest))
          .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void givenResetPasswordRequired_whenLogin_thenReturns401WithResetCodeId() {
      // Given
      var authState = new PlatformAuthorizationSessionState(
          "http://localhost:5173/callback", "openid", "challenge", "S256");
      when(httpRequest.getSession(false)).thenReturn(httpSession);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_STATE))
          .thenReturn(authState);
      when(authenticateUseCase.execute(any(AuthenticatePlatformUserCommand.class)))
          .thenThrow(new UserPasswordResetRequiredException("platform_admin"));

      UUID resetId = UUID.randomUUID();
      when(sendPlatformPasswordResetCodeUseCase.execute("admin@keygo.local"))
          .thenReturn(new io.cmartinezs.keygo.app.user.result.SendPasswordResetCodeResult(resetId));

      var request = new PlatformLoginRequest("admin@keygo.local", "temp123");

      // When
      var response = controller.login(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getFailure()).isNotNull();
      assertThat(response.getBody().getFailure().getCode())
          .isEqualTo(ResponseCode.RESET_PASSWORD_REQUIRED.name());
      assertThat(response.getBody().getData()).isNotNull();
      assertThat(response.getBody().getData().resetCodeId()).isEqualTo(resetId.toString());
    }
  }

  // ─── Token (authorization_code grant) tests ─────────────────────────────────

  @Nested
  class AuthorizationCodeGrantTests {

    private String codeVerifier;
    private String codeChallenge;

    @BeforeEach
    void setUpPkce() {
      codeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
      codeChallenge = PlatformAuthController.computeS256Challenge(codeVerifier);
    }

    @Test
    void givenValidCodeAndVerifier_whenToken_thenReturnsTokens() {
      // Given
      String code = UUID.randomUUID().toString();
      var authState = new PlatformAuthorizationSessionState(
          "http://localhost:5173/callback", "openid", codeChallenge, "S256");
      var authCodeState = new PlatformAuthCodeState(code, userId, Instant.now());

      when(httpRequest.getSession(false)).thenReturn(httpSession);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_STATE))
          .thenReturn(authState);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_CODE))
          .thenReturn(authCodeState);
      when(getPlatformUserUseCase.execute(UserId.of(userId)))
          .thenReturn(activePlatformUser);
      when(issueTokensUseCase.execute(eq(activePlatformUser), anyString(), any(), any()))
          .thenReturn(new IssuePlatformTokensResult(
              "access.jwt", "refresh.token", "Bearer", 3600L, "key-1"));
      when(httpRequest.getScheme()).thenReturn("http");
      when(httpRequest.getServerName()).thenReturn("localhost");
      when(httpRequest.getServerPort()).thenReturn(8080);
      when(httpRequest.getContextPath()).thenReturn("/keygo-server");
      when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

      var request = new PlatformTokenRequest(
          "authorization_code", null, code,
          "http://localhost:5173/callback", codeVerifier);

      // When
      var response = controller.token(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().getSuccess().getCode())
          .isEqualTo(ResponseCode.PLATFORM_TOKEN_ISSUED.getCode());

      PlatformTokenData data = response.getBody().getData();
      assertThat(data.accessToken()).isEqualTo("access.jwt");
      assertThat(data.refreshToken()).isEqualTo("refresh.token");

      // Session attributes invalidated (single-use code)
      verify(httpSession).removeAttribute(PlatformAuthController.SESSION_ATTR_AUTH_CODE);
      verify(httpSession).removeAttribute(PlatformAuthController.SESSION_ATTR_AUTH_STATE);
    }

    @Test
    void givenWrongCode_whenToken_thenReturnsUnauthorized() {
      // Given
      var authState = new PlatformAuthorizationSessionState(
          "http://localhost:5173/callback", "openid", codeChallenge, "S256");
      var authCodeState = new PlatformAuthCodeState("correct-code", userId, Instant.now());

      when(httpRequest.getSession(false)).thenReturn(httpSession);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_STATE))
          .thenReturn(authState);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_CODE))
          .thenReturn(authCodeState);

      var request = new PlatformTokenRequest(
          "authorization_code", null, "wrong-code", null, codeVerifier);

      // When
      var response = controller.token(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenWrongCodeVerifier_whenToken_thenReturnsUnauthorized() {
      // Given
      String code = UUID.randomUUID().toString();
      var authState = new PlatformAuthorizationSessionState(
          "http://localhost:5173/callback", "openid", codeChallenge, "S256");
      var authCodeState = new PlatformAuthCodeState(code, userId, Instant.now());

      when(httpRequest.getSession(false)).thenReturn(httpSession);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_STATE))
          .thenReturn(authState);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_CODE))
          .thenReturn(authCodeState);

      var request = new PlatformTokenRequest(
          "authorization_code", null, code, null, "wrong-verifier");

      // When
      var response = controller.token(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenMissingCodeVerifier_whenToken_thenReturnsUnauthorized() {
      // Given
      String code = UUID.randomUUID().toString();
      var authState = new PlatformAuthorizationSessionState(
          "http://localhost:5173/callback", "openid", codeChallenge, "S256");
      var authCodeState = new PlatformAuthCodeState(code, userId, Instant.now());

      when(httpRequest.getSession(false)).thenReturn(httpSession);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_STATE))
          .thenReturn(authState);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_CODE))
          .thenReturn(authCodeState);

      var request = new PlatformTokenRequest(
          "authorization_code", null, code, null, null);

      // When
      var response = controller.token(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenNoSession_whenToken_thenReturnsUnauthorized() {
      // Given
      when(httpRequest.getSession(false)).thenReturn(null);
      var request = new PlatformTokenRequest(
          "authorization_code", null, "code", null, codeVerifier);

      // When
      var response = controller.token(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenMismatchedRedirectUri_whenToken_thenReturnsUnauthorized() {
      // Given
      String code = UUID.randomUUID().toString();
      var authState = new PlatformAuthorizationSessionState(
          "http://localhost:5173/callback", "openid", codeChallenge, "S256");
      var authCodeState = new PlatformAuthCodeState(code, userId, Instant.now());

      when(httpRequest.getSession(false)).thenReturn(httpSession);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_STATE))
          .thenReturn(authState);
      when(httpSession.getAttribute(PlatformAuthController.SESSION_ATTR_AUTH_CODE))
          .thenReturn(authCodeState);

      var request = new PlatformTokenRequest(
          "authorization_code", null, code,
          "http://evil.com/callback", codeVerifier);

      // When
      var response = controller.token(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
  }

  // ─── Token (refresh_token grant) tests ──────────────────────────────────────

  @Nested
  class RefreshTokenGrantTests {

    @Test
    void givenValidRefreshToken_whenTokenRotation_thenReturnsNewTokens() {
      // Given
      var request = new PlatformTokenRequest(
          "refresh_token", "raw-refresh-token", null, null, null);
      when(rotateRefreshTokenUseCase.execute(any(RotatePlatformRefreshTokenCommand.class)))
          .thenReturn(new IssuePlatformTokensResult(
              "new-access.jwt", "new-refresh.token", "Bearer", 3600L, "key-2"));

      // When
      var response = controller.token(request, httpRequest);

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
    void givenMissingRefreshToken_whenTokenRotation_thenReturnsBadRequest() {
      // Given
      var request = new PlatformTokenRequest(
          "refresh_token", null, null, null, null);

      // When
      var response = controller.token(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody().getFailure().getCode())
          .isEqualTo(ResponseCode.REQUIRED_FIELD_MISSING.getCode());
    }
  }

  // ─── Token (unsupported grant) tests ────────────────────────────────────────

  @Test
  void givenUnsupportedGrantType_whenToken_thenReturnsBadRequest() {
    // Given
    var request = new PlatformTokenRequest(
        "client_credentials", null, null, null, null);

    // When
    var response = controller.token(request, httpRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.INVALID_INPUT.getCode());
  }

  // ─── Direct login tests ─────────────────────────────────────────────────────

  @Nested
  class DirectLoginTests {

    @Test
    void givenValidCredentials_whenDirectLogin_thenReturnsTokens() {
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
      var response = controller.directLogin(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();

      @SuppressWarnings("unchecked")
      var body = (BaseResponse<PlatformTokenData>) response.getBody();
      assertThat(body.getSuccess().getCode())
          .isEqualTo(ResponseCode.PLATFORM_TOKEN_ISSUED.getCode());

      PlatformTokenData data = body.getData();
      assertThat(data.accessToken()).isEqualTo("access.jwt");
      assertThat(data.refreshToken()).isEqualTo("refresh.token");
      assertThat(data.tokenType()).isEqualTo("Bearer");
      assertThat(data.expiresIn()).isEqualTo(3600L);
    }

    @Test
    void givenInvalidPassword_whenDirectLogin_thenThrowsInvalidCredentials() {
      // Given
      var request = new PlatformLoginRequest("admin@keygo.local", "wrong-password");
      when(authenticateUseCase.execute(any(AuthenticatePlatformUserCommand.class)))
          .thenThrow(new InvalidCredentialsException());

      // When / Then
      assertThatThrownBy(() -> controller.directLogin(request, httpRequest))
          .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void givenUnknownEmail_whenDirectLogin_thenThrowsUserNotFound() {
      // Given
      var request = new PlatformLoginRequest("unknown@keygo.local", "password");
      when(authenticateUseCase.execute(any(AuthenticatePlatformUserCommand.class)))
          .thenThrow(new UserNotFoundException("email", "unknown@keygo.local"));

      // When / Then
      assertThatThrownBy(() -> controller.directLogin(request, httpRequest))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void givenResetPasswordRequired_whenDirectLogin_thenReturns401WithResetCodeId() {
      // Given
      when(authenticateUseCase.execute(any(AuthenticatePlatformUserCommand.class)))
          .thenThrow(new UserPasswordResetRequiredException("platform_admin"));

      UUID resetId = UUID.randomUUID();
      when(sendPlatformPasswordResetCodeUseCase.execute("admin@keygo.local"))
          .thenReturn(new io.cmartinezs.keygo.app.user.result.SendPasswordResetCodeResult(resetId));

      var request = new PlatformLoginRequest("admin@keygo.local", "temp123");

      // When
      var response = controller.directLogin(request, httpRequest);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody()).isNotNull();

      @SuppressWarnings("unchecked")
      var body = (BaseResponse<PlatformLoginData>) response.getBody();
      assertThat(body.getFailure()).isNotNull();
      assertThat(body.getFailure().getCode())
          .isEqualTo(ResponseCode.RESET_PASSWORD_REQUIRED.name());
      assertThat(body.getData()).isNotNull();
      assertThat(body.getData().resetCodeId()).isEqualTo(resetId.toString());
    }

    @Test
    void givenSuspendedUser_whenDirectLogin_thenThrowsUserSuspended() {
      // Given
      var request = new PlatformLoginRequest("suspended@keygo.local", "Admin1234!");
      when(authenticateUseCase.execute(any(AuthenticatePlatformUserCommand.class)))
          .thenThrow(new UserSuspendedException("suspended_admin"));

      // When / Then
      assertThatThrownBy(() -> controller.directLogin(request, httpRequest))
          .isInstanceOf(UserSuspendedException.class);
    }
  }

  // ─── PKCE helper tests ──────────────────────────────────────────────────────

  @Test
  void computeS256Challenge_producesExpectedHash() {
    // Given — known verifier
    String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";

    // When
    String challenge = PlatformAuthController.computeS256Challenge(verifier);

    // Then — should be deterministic and non-empty
    assertThat(challenge).isNotBlank();
    // Same input always produces same output
    assertThat(PlatformAuthController.computeS256Challenge(verifier)).isEqualTo(challenge);
    // Different input produces different output
    assertThat(PlatformAuthController.computeS256Challenge("different-verifier"))
        .isNotEqualTo(challenge);
  }
}
