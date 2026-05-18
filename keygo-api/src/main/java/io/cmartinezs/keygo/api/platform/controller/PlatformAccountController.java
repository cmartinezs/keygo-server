package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.membership.response.AppAccessData;
import io.cmartinezs.keygo.api.membership.response.TenantAccessData;
import io.cmartinezs.keygo.api.platform.request.PlatformRevokeTokenRequest;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.user.request.AccountResetPasswordRequest;
import io.cmartinezs.keygo.api.user.request.ForgotPasswordRequest;
import io.cmartinezs.keygo.api.user.request.RecoverPasswordRequest;
import io.cmartinezs.keygo.api.user.request.UpdateUserProfileRequest;
import io.cmartinezs.keygo.api.user.response.UserProfileData;
import io.cmartinezs.keygo.app.auth.command.RevokeTokenCommand;
import io.cmartinezs.keygo.app.auth.usecase.RevokeTokenUseCase;
import io.cmartinezs.keygo.app.membership.result.TenantAccessResult;
import io.cmartinezs.keygo.app.membership.usecase.GetAccountAccessUseCase;
import io.cmartinezs.keygo.app.user.command.GetPlatformUserProfileCommand;
import io.cmartinezs.keygo.app.user.command.UpdatePlatformUserProfileCommand;
import io.cmartinezs.keygo.app.user.result.ForgotPasswordResult;
import io.cmartinezs.keygo.app.user.result.RecoverPasswordResult;
import io.cmartinezs.keygo.app.user.result.ResetPasswordResult;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
import io.cmartinezs.keygo.app.user.usecase.ForgotPlatformPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetPlatformUserProfileUseCase;
import io.cmartinezs.keygo.app.user.usecase.RecoverPlatformPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResetPlatformPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdatePlatformUserProfileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para operaciones de cuenta de plataforma (self-service).
 *
 * <p>Endpoints públicos que no requieren Bearer token:
 * <ul>
 *   <li>POST /account/forgot-password — solicitar token de recuperación</li>
 *   <li>POST /account/recover-password — restablecer contraseña con token</li>
 *   <li>POST /account/reset-password — restablecer contraseña con contraseña temporal + código</li>
 *   <li>POST /oauth2/revoke — revocar token (RFC 7009)</li>
 * </ul>
 *
 * <p>Endpoints que requieren Bearer token:
 * <ul>
 *   <li>GET  /account/profile — consultar perfil propio del platform user</li>
 *   <li>PATCH /account/profile — actualizar perfil propio del platform user</li>
 * </ul>
 *
 * <p>Estos endpoints son exclusivos de keygo-UI y están protegidos por CORS.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/platform")
@Tag(name = "Platform Account",
    description = "Platform account self-service endpoints — profile management, "
        + "forgot/recover/reset password and token revocation")
public class PlatformAccountController {

  private final ForgotPlatformPasswordUseCase forgotPasswordUseCase;
  private final RecoverPlatformPasswordUseCase recoverPasswordUseCase;
  private final ResetPlatformPasswordUseCase resetPasswordUseCase;
  private final RevokeTokenUseCase revokeTokenUseCase;
  private final GetPlatformUserProfileUseCase getPlatformUserProfileUseCase;
  private final UpdatePlatformUserProfileUseCase updatePlatformUserProfileUseCase;
  private final GetAccountAccessUseCase getAccountAccessUseCase;

  public PlatformAccountController(
      ForgotPlatformPasswordUseCase forgotPasswordUseCase,
      RecoverPlatformPasswordUseCase recoverPasswordUseCase,
      ResetPlatformPasswordUseCase resetPasswordUseCase,
      RevokeTokenUseCase revokeTokenUseCase,
      GetPlatformUserProfileUseCase getPlatformUserProfileUseCase,
      UpdatePlatformUserProfileUseCase updatePlatformUserProfileUseCase,
      GetAccountAccessUseCase getAccountAccessUseCase) {
    this.forgotPasswordUseCase = forgotPasswordUseCase;
    this.recoverPasswordUseCase = recoverPasswordUseCase;
    this.resetPasswordUseCase = resetPasswordUseCase;
    this.revokeTokenUseCase = revokeTokenUseCase;
    this.getPlatformUserProfileUseCase = getPlatformUserProfileUseCase;
    this.updatePlatformUserProfileUseCase = updatePlatformUserProfileUseCase;
    this.getAccountAccessUseCase = getAccountAccessUseCase;
  }

  // ─── POST /account/forgot-password ──────────────────────────────────────────

  @PostMapping("/account/forgot-password")
  @Operation(
      summary = "Request platform password recovery token",
      description = "Sends a one-time password recovery token to the platform user's email. "
          + "Always returns 200 regardless of whether the email exists (anti-enumeration).")
  @ApiResponse(responseCode = "200",
      description = "Recovery email sent (or email not found — not revealed) (code: ACCOUNT_PASSWORD_RECOVERY_SENT)")
  public ResponseEntity<BaseResponse<ForgotPasswordResult>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {

    ForgotPasswordResult result = forgotPasswordUseCase.execute(request.email());

    return ResponseEntity.ok(
        BaseResponse.<ForgotPasswordResult>builder()
            .data(result)
            .success(ResponseHelper.message(ResponseCode.ACCOUNT_PASSWORD_RECOVERY_SENT))
            .build());
  }

  // ─── POST /account/recover-password ─────────────────────────────────────────

  @PostMapping("/account/recover-password")
  @Operation(
      summary = "Recover platform password with recovery token",
      description = "Sets a new password using the one-time recovery token received by email. "
          + "The token is invalidated after use.")
  @ApiResponse(responseCode = "200",
      description = "Password recovered successfully (code: ACCOUNT_PASSWORD_RECOVERED)")
  @ApiResponse(responseCode = "400",
      description = "New password violates policy or passwords don't match (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404",
      description = "Recovery token not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "422",
      description = "Token expired or already used (code: BUSINESS_RULE_VIOLATION)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<RecoverPasswordResult>> recoverPassword(
      @Valid @RequestBody RecoverPasswordRequest request) {

    RecoverPasswordResult result = recoverPasswordUseCase.execute(
        request.recoveryToken(), request.newPassword());

    return ResponseEntity.ok(
        BaseResponse.<RecoverPasswordResult>builder()
            .data(result)
            .success(ResponseHelper.message(ResponseCode.ACCOUNT_PASSWORD_RECOVERED))
            .build());
  }

  // ─── POST /account/reset-password ───────────────────────────────────────────

  @PostMapping("/account/reset-password")
  @Operation(
      summary = "Reset platform password with temporary password",
      description = "Allows a platform user in RESET_PASSWORD status to set their permanent password "
          + "using the temporary password, request_id and verification code.")
  @ApiResponse(responseCode = "200",
      description = "Password reset successfully (code: ACCOUNT_PASSWORD_RESET)")
  @ApiResponse(responseCode = "400",
      description = "Validation failed (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "403",
      description = "Temporary password incorrect or user not in RESET_PASSWORD status",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404",
      description = "Reset request not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "422",
      description = "Verification code expired (code: BUSINESS_RULE_VIOLATION)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<ResetPasswordResult>> resetPassword(
      @Valid @RequestBody AccountResetPasswordRequest request) {

    ResetPasswordResult result = resetPasswordUseCase.execute(
        request.requestId(),
        request.temporaryPassword(),
        request.newPassword(),
        request.confirmNewPassword(),
        request.verificationCode());

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<ResetPasswordResult>builder()
            .data(result)
            .success(ResponseHelper.message(ResponseCode.ACCOUNT_PASSWORD_RESET))
            .build());
  }

  // ─── POST /oauth2/revoke ──────────────────────────────────────────────────

  @PostMapping("/oauth2/revoke")
  @Operation(
      summary = "Revoke a platform token (RFC 7009)",
      description = "Revokes a refresh token per RFC 7009. Idempotent — returns 200 even if "
          + "the token does not exist or has already been revoked.")
  @ApiResponse(responseCode = "200",
      description = "Token revoked — or silently ignored if unknown (code: TOKEN_REVOKED)")
  @ApiResponse(responseCode = "400",
      description = "Request body validation failed (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<Void>> revoke(
      @Valid @RequestBody PlatformRevokeTokenRequest request) {

    revokeTokenUseCase.execute(
        new RevokeTokenCommand(null, null, request.token(), request.tokenTypeHint()));

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<Void>builder()
            .success(ResponseHelper.message(ResponseCode.TOKEN_REVOKED))
            .build());
  }

  // ─── GET /account/profile ─────────────────────────────────────────────────

  /**
   * GET /api/v1/platform/account/profile
   *
   * <p>Retorna el perfil completo del usuario de plataforma autenticado.
   *
   * @return perfil completo del platform user
   */
  @GetMapping("/account/profile")
  @Operation(
      summary = "Get own platform profile",
      description = "Returns the complete profile of the authenticated platform user. "
          + "Requires Authorization: Bearer <access_token>.")
  @ApiResponse(responseCode = "200",
      description = "Profile retrieved successfully (code: USER_PROFILE_RETRIEVED)")
  @ApiResponse(responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404",
      description = "Platform user not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<UserProfileData>> getProfile() {

    UserProfileResult result = getPlatformUserProfileUseCase.execute(
        new GetPlatformUserProfileCommand(extractUserId()));

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<UserProfileData>builder()
            .data(toData(result))
            .success(ResponseHelper.message(ResponseCode.USER_PROFILE_RETRIEVED))
            .build());
  }

  // ─── PATCH /account/profile ───────────────────────────────────────────────

  /**
   * PATCH /api/v1/platform/account/profile
   *
   * <p>Actualiza parcialmente el perfil del usuario de plataforma autenticado.
   * Solo se actualizan los campos enviados (no-nulos) — semántica PATCH.
   *
   * @param request campos de perfil a actualizar (todos opcionales)
   * @return perfil actualizado del platform user
   */
  @PatchMapping("/account/profile")
  @Operation(
      summary = "Update own platform profile",
      description = "Partially updates the profile of the authenticated platform user. "
          + "Only non-null fields are updated (PATCH semantics). "
          + "Requires Authorization: Bearer <access_token>.")
  @ApiResponse(responseCode = "200",
      description = "Profile updated successfully (code: USER_PROFILE_UPDATED)")
  @ApiResponse(responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404",
      description = "Platform user not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<UserProfileData>> updateProfile(
      @Valid @RequestBody UpdateUserProfileRequest request) {

    UserProfileResult result = updatePlatformUserProfileUseCase.execute(
        new UpdatePlatformUserProfileCommand(
            extractUserId(),
            request.firstName(), request.lastName(),
            request.phoneNumber(), request.locale(), request.zoneinfo(),
            request.profilePictureUrl()));

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<UserProfileData>builder()
            .data(toData(result))
            .success(ResponseHelper.message(ResponseCode.USER_PROFILE_UPDATED))
            .build());
  }

  // ─── GET /account/access ──────────────────────────────────────────────────

  @GetMapping("/account/access")
  @PreAuthorize("isAuthenticated()")
  @io.swagger.v3.oas.annotations.Operation(
      summary = "Get own platform access",
      description = "Returns all tenants and apps the authenticated platform user has memberships in, "
          + "grouped by tenant. Derived entirely from the Bearer token — no extra parameters needed.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
      description = "Access view retrieved (code: ACCOUNT_ACCESS_RETRIEVED)")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @io.swagger.v3.oas.annotations.media.Content(
          schema = @io.swagger.v3.oas.annotations.media.Schema(
              implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<List<TenantAccessData>>> getAccess() {
    UUID userId = UUID.fromString(extractUserId());

    List<TenantAccessResult> results = getAccountAccessUseCase.execute(userId);

    List<TenantAccessData> data = results.stream()
        .map(r -> TenantAccessData.builder()
            .tenantId(r.tenantId())
            .tenantSlug(r.tenantSlug())
            .tenantName(r.tenantName())
            .apps(r.apps().stream()
                .map(a -> AppAccessData.builder()
                    .membershipId(a.membershipId())
                    .membershipStatus(a.membershipStatus())
                    .clientAppId(a.clientAppId())
                    .clientId(a.clientId())
                    .appName(a.appName())
                    .roles(a.roles())
                    .build())
                .toList())
            .build())
        .toList();

    return ResponseEntity.ok(
        BaseResponse.<List<TenantAccessData>>builder()
            .data(data)
            .success(ResponseHelper.message(ResponseCode.ACCOUNT_ACCESS_RETRIEVED))
            .build());
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

  @SuppressWarnings("unchecked")
  private String extractUserId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    return (String) ((Map<String, Object>) auth.getPrincipal()).get("sub");
  }

  private UserProfileData toData(UserProfileResult result) {
    return UserProfileData.builder()
        .id(result.id())
        .tenantId(result.tenantId())
        .username(result.username())
        .email(result.email())
        .firstName(result.firstName())
        .lastName(result.lastName())
        .status(result.status())
        .phoneNumber(result.phoneNumber())
        .locale(result.locale())
        .zoneinfo(result.zoneinfo())
        .profilePictureUrl(result.profilePictureUrl())
        .birthdate(result.birthdate())
        .website(result.website())
        .build();
  }
}
