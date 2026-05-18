package io.cmartinezs.keygo.api.user.controller;

import io.cmartinezs.keygo.api.error.UnauthorizedException;
import io.cmartinezs.keygo.api.user.request.AccountResetPasswordRequest;
import io.cmartinezs.keygo.api.user.request.ChangePasswordRequest;
import io.cmartinezs.keygo.api.user.request.ForgotPasswordRequest;
import io.cmartinezs.keygo.api.user.request.RecoverPasswordRequest;
import io.cmartinezs.keygo.app.user.result.ChangePasswordResult;
import io.cmartinezs.keygo.app.user.result.ForgotPasswordResult;
import io.cmartinezs.keygo.app.user.result.ListUserSessionsResult;
import io.cmartinezs.keygo.app.user.result.RecoverPasswordResult;
import io.cmartinezs.keygo.app.user.result.ResetPasswordResult;
import io.cmartinezs.keygo.app.user.result.SessionInfoResult;
import io.cmartinezs.keygo.app.user.usecase.ChangePasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.ForgotPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetNotificationPreferencesUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetUserAccessUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListUserSessionsUseCase;
import io.cmartinezs.keygo.app.user.usecase.RecoverPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResetPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.RevokeUserSessionUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateNotificationPreferencesUseCase;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountSettingsControllerTest {

  private static final String TENANT_SLUG = "acme";

  @Mock ForgotPasswordUseCase forgotPasswordUseCase;
  @Mock RecoverPasswordUseCase recoverPasswordUseCase;
  @Mock ResetPasswordUseCase resetPasswordUseCase;
  @Mock ChangePasswordUseCase changePasswordUseCase;
  @Mock ListUserSessionsUseCase listUserSessionsUseCase;
  @Mock RevokeUserSessionUseCase revokeUserSessionUseCase;
  @Mock GetNotificationPreferencesUseCase getNotificationPreferencesUseCase;
  @Mock UpdateNotificationPreferencesUseCase updateNotificationPreferencesUseCase;
  @Mock GetUserAccessUseCase getUserAccessUseCase;

  private AccountSettingsController controller;

  @BeforeEach
  void setUp() {
    controller = new AccountSettingsController(
        forgotPasswordUseCase, recoverPasswordUseCase, resetPasswordUseCase,
        changePasswordUseCase, listUserSessionsUseCase, revokeUserSessionUseCase,
        getNotificationPreferencesUseCase, updateNotificationPreferencesUseCase,
        getUserAccessUseCase);
  }

  @Test
  void forgotPassword_returns200WithSentTrue() {
    // Given
    when(forgotPasswordUseCase.execute(any())).thenReturn(new ForgotPasswordResult(true, "j********n@a****.com"));

    // When
    ResponseEntity<?> response = controller.forgotPassword(
        TENANT_SLUG, new ForgotPasswordRequest("john@acme.com"));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void recoverPassword_returns200WithRecoveredTrue() {
    // Given
    when(recoverPasswordUseCase.execute(any())).thenReturn(new RecoverPasswordResult(true));

    // When
    ResponseEntity<?> response = controller.recoverPassword(
        TENANT_SLUG,
        new RecoverPasswordRequest("abc123token", "NewSecure@2026!", "NewSecure@2026!"));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void recoverPassword_passwordMatchValidation_returnsFalseOnMismatch() {
    // Given
    var request = new RecoverPasswordRequest("abc123token", "NewSecure@2026!", "DifferentPass!");

    // Then — Bean Validation @AssertTrue
    assertThat(request.isPasswordMatch()).isFalse();
  }

  @Test
  void recoverPassword_passwordMatchValidation_returnsTrueOnMatch() {
    // Given
    var request = new RecoverPasswordRequest("abc123token", "NewSecure@2026!", "NewSecure@2026!");

    // Then
    assertThat(request.isPasswordMatch()).isTrue();
  }

  @Test
  void resetPassword_returns200WithResetTrue() {
    // Given
    when(resetPasswordUseCase.execute(any())).thenReturn(new ResetPasswordResult(true));

    // When
    ResponseEntity<?> response = controller.resetPassword(
        TENANT_SLUG,
        new AccountResetPasswordRequest(
            "john@acme.com", "TempPass@123", "NewSecure@2026!", "NewSecure@2026!", "123456"));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void changePassword_returns200WithBearerToken() {
    // Given
    when(changePasswordUseCase.execute(any())).thenReturn(new ChangePasswordResult(true));

    // When
    ResponseEntity<?> response = controller.changePassword(
        TENANT_SLUG,
        "Bearer sometoken",
        new ChangePasswordRequest("oldPass@123", "NewPass@456"));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void changePassword_throwsUnauthorizedWhenBearerMissing() {
    // When / Then
    assertThatThrownBy(() ->
        controller.changePassword(TENANT_SLUG, null, new ChangePasswordRequest("old", "new")))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test
  void changePassword_throwsUnauthorizedWhenBearerPrefixMissing() {
    // When / Then
    assertThatThrownBy(() ->
        controller.changePassword(TENANT_SLUG, "Basic abc", new ChangePasswordRequest("old", "new")))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test
  void logout_withActiveCurrentSession_revokesSessionAndReturns200() {
    // Given
    UUID sessionId = UUID.randomUUID();
    SessionInfoResult currentSession = new SessionInfoResult(
        sessionId, "ACTIVE", "Mozilla/5.0", "127.0.0.1",
        Instant.now(), Instant.now(), Instant.now().plusSeconds(3600), true);
    ListUserSessionsResult sessions = new ListUserSessionsResult(List.of(currentSession));

    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    when(listUserSessionsUseCase.execute(any())).thenReturn(sessions);

    // When
    ResponseEntity<?> response = controller.logout(TENANT_SLUG, "Bearer sometoken", httpRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(revokeUserSessionUseCase).execute(any());
  }

  @Test
  void logout_withNoCurrentSession_returns200Anyway() {
    // Given — session exists but isCurrent=false
    SessionInfoResult otherSession = new SessionInfoResult(
        UUID.randomUUID(), "ACTIVE", "OtherAgent", "10.0.0.1",
        Instant.now(), Instant.now(), Instant.now().plusSeconds(3600), false);
    ListUserSessionsResult sessions = new ListUserSessionsResult(List.of(otherSession));

    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    when(listUserSessionsUseCase.execute(any())).thenReturn(sessions);

    // When
    ResponseEntity<?> response = controller.logout(TENANT_SLUG, "Bearer sometoken", httpRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(revokeUserSessionUseCase, never()).execute(any());
  }

  @Test
  void logout_throwsUnauthorizedWhenBearerMissing() {
    // Given
    HttpServletRequest httpRequest = mock(HttpServletRequest.class);

    // When / Then
    assertThatThrownBy(() ->
        controller.logout(TENANT_SLUG, null, httpRequest))
        .isInstanceOf(UnauthorizedException.class);
  }
}
