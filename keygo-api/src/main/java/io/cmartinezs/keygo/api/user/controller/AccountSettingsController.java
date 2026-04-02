package io.cmartinezs.keygo.api.user.controller;

import io.cmartinezs.keygo.api.error.UnauthorizedException;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.user.request.ChangePasswordRequest;
import io.cmartinezs.keygo.api.user.request.UpdateNotificationPreferencesRequest;
import io.cmartinezs.keygo.api.user.response.AccountSessionData;
import io.cmartinezs.keygo.api.user.response.NotificationPreferencesData;
import io.cmartinezs.keygo.api.user.response.UserAccessData;
import io.cmartinezs.keygo.app.user.command.ChangePasswordCommand;
import io.cmartinezs.keygo.app.user.command.GetNotificationPreferencesCommand;
import io.cmartinezs.keygo.app.user.command.GetUserAccessCommand;
import io.cmartinezs.keygo.app.user.command.ListUserSessionsCommand;
import io.cmartinezs.keygo.app.user.command.RevokeUserSessionCommand;
import io.cmartinezs.keygo.app.user.command.UpdateNotificationPreferencesCommand;
import io.cmartinezs.keygo.app.user.result.ChangePasswordResult;
import io.cmartinezs.keygo.app.user.result.ListUserSessionsResult;
import io.cmartinezs.keygo.app.user.result.NotificationPreferencesResult;
import io.cmartinezs.keygo.app.user.result.RevokeUserSessionResult;
import io.cmartinezs.keygo.app.user.result.UserAccessResult;
import io.cmartinezs.keygo.app.user.usecase.ChangePasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetNotificationPreferencesUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetUserAccessUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListUserSessionsUseCase;
import io.cmartinezs.keygo.app.user.usecase.RevokeUserSessionUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateNotificationPreferencesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller: configuración y seguridad de la cuenta del usuario autenticado (self-service).
 *
 * <p>Todos los endpoints requieren {@code Authorization: Bearer <access_token>}.
 * No requieren {@code X-KEYGO-ADMIN} — son endpoints de usuario final.
 * El filtro {@code BootstrapAdminKeyFilter} los declara públicos; la validación del Bearer
 * se realiza dentro de cada caso de uso.
 *
 * <p>Endpoints activos por fase:
 * <ul>
 *   <li>Fase 2: POST /change-password</li>
 *   <li>Fase 3: GET /sessions, DELETE /sessions/{sessionId}</li>
 *   <li>Fase 4: GET /notification-preferences, PATCH /notification-preferences</li>
 *   <li>Fase 5: GET /access</li>
 * </ul>
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/account")
@Tag(name = "Account Settings", description = "Self-service account security and settings — requires Bearer token (no X-KEYGO-ADMIN)")
public class AccountSettingsController {

  private final ChangePasswordUseCase changePasswordUseCase;
  private final ListUserSessionsUseCase listUserSessionsUseCase;
  private final RevokeUserSessionUseCase revokeUserSessionUseCase;
  private final GetNotificationPreferencesUseCase getNotificationPreferencesUseCase;
  private final UpdateNotificationPreferencesUseCase updateNotificationPreferencesUseCase;
  private final GetUserAccessUseCase getUserAccessUseCase;

  public AccountSettingsController(
      ChangePasswordUseCase changePasswordUseCase,
      ListUserSessionsUseCase listUserSessionsUseCase,
      RevokeUserSessionUseCase revokeUserSessionUseCase,
      GetNotificationPreferencesUseCase getNotificationPreferencesUseCase,
      UpdateNotificationPreferencesUseCase updateNotificationPreferencesUseCase,
      GetUserAccessUseCase getUserAccessUseCase) {
    this.changePasswordUseCase = changePasswordUseCase;
    this.listUserSessionsUseCase = listUserSessionsUseCase;
    this.revokeUserSessionUseCase = revokeUserSessionUseCase;
    this.getNotificationPreferencesUseCase = getNotificationPreferencesUseCase;
    this.updateNotificationPreferencesUseCase = updateNotificationPreferencesUseCase;
    this.getUserAccessUseCase = getUserAccessUseCase;
  }

  /**
   * POST /api/v1/tenants/{tenantSlug}/account/change-password
   *
   * <p>Permite al usuario autenticado cambiar su propia contraseña.
   * Requiere la contraseña actual para verificación antes de aplicar la nueva.
   *
   * @param tenantSlug    slug del tenant
   * @param authorization header Authorization (debe ser "Bearer &lt;token&gt;")
   * @param request       body con {@code current_password} y {@code new_password}
   * @return {@code { "changed": true }} si el cambio fue exitoso
   */
  @PostMapping("/change-password")
  @Operation(
      summary = "Change own password",
      description = "Allows the authenticated user to rotate their own password. "
                    + "Requires the current password for verification. "
                    + "Requires Authorization: Bearer <access_token>.")
  @ApiResponse(responseCode = "200",
      description = "Password changed successfully (code: ACCOUNT_PASSWORD_CHANGED)")
  @ApiResponse(responseCode = "400",
      description = "Validation error — new password violates policy (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "403",
      description = "Current password is incorrect (code: BUSINESS_RULE_VIOLATION)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "500",
      description = "Internal error (code: OPERATION_FAILED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "503",
      description = "External service unavailable (code: EXTERNAL_SERVICE_ERROR)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<ChangePasswordResult>> changePassword(
      @Parameter(description = "Tenant slug", example = "my-company")
      @PathVariable String tenantSlug,
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @RequestBody ChangePasswordRequest request) {

    String bearerToken = extractBearerToken(authorization);

    ChangePasswordResult result = changePasswordUseCase.execute(
        new ChangePasswordCommand(
            tenantSlug,
            bearerToken,
            request.currentPassword(),
            request.newPassword()));

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<ChangePasswordResult>builder()
            .data(result)
            .success(ResponseHelper.message(ResponseCode.ACCOUNT_PASSWORD_CHANGED))
            .build());
  }

  /**
   * GET /api/v1/tenants/{tenantSlug}/account/sessions
   *
   * <p>Devuelve las sesiones activas del usuario autenticado.
   * La sesión actual se marca con {@code is_current = true}.
   *
   * @param tenantSlug    slug del tenant
   * @param authorization header Authorization (debe ser "Bearer &lt;token&gt;")
   * @param httpRequest   petición HTTP (para extraer User-Agent e IP actuales)
   * @return lista de sesiones activas con metadatos de dispositivo
   */
  @GetMapping("/sessions")
  @Operation(
      summary = "List own active sessions",
      description = "Returns all ACTIVE sessions of the authenticated user. "
                    + "The current session is identified by matching User-Agent + IP address. "
                    + "Requires Authorization: Bearer <access_token>.")
  @ApiResponse(responseCode = "200",
      description = "Sessions retrieved successfully (code: ACCOUNT_SESSIONS_RETRIEVED)")
  @ApiResponse(responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "500",
      description = "Internal error (code: OPERATION_FAILED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<List<AccountSessionData>>> listSessions(
      @Parameter(description = "Tenant slug", example = "my-company")
      @PathVariable String tenantSlug,
      @RequestHeader(value = "Authorization", required = false) String authorization,
      HttpServletRequest httpRequest) {

    String bearerToken = extractBearerToken(authorization);
    String userAgent = httpRequest.getHeader("User-Agent");
    String ipAddress = extractClientIp(httpRequest);

    ListUserSessionsResult result = listUserSessionsUseCase.execute(
        new ListUserSessionsCommand(tenantSlug, bearerToken, userAgent, ipAddress));

    List<AccountSessionData> data = result.sessions().stream()
        .map(AccountSessionData::from)
        .toList();

    return ResponseEntity.ok(
        BaseResponse.<List<AccountSessionData>>builder()
            .data(data)
            .success(ResponseHelper.message(ResponseCode.ACCOUNT_SESSIONS_RETRIEVED))
            .build());
  }

  /**
   * DELETE /api/v1/tenants/{tenantSlug}/account/sessions/{sessionId}
   *
   * <p>Revoca (cierra) una sesión propia del usuario autenticado.
   * Operación idempotente: si la sesión no existe o ya está cerrada, devuelve {@code alreadyClosed=true} con 200.
   *
   * @param tenantSlug    slug del tenant
   * @param sessionId     UUID de la sesión a revocar
   * @param authorization header Authorization (debe ser "Bearer &lt;token&gt;")
   * @return resultado con {@code already_closed} indicando si ya estaba cerrada
   */
  @DeleteMapping("/sessions/{sessionId}")
  @Operation(
      summary = "Revoke own session",
      description = "Closes a session belonging to the authenticated user. "
                    + "Idempotent: returns already_closed=true if the session was already closed or not found. "
                    + "Requires Authorization: Bearer <access_token>.")
  @ApiResponse(responseCode = "200",
      description = "Session revoked or already closed (code: ACCOUNT_SESSION_REVOKED or ACCOUNT_SESSION_ALREADY_CLOSED)")
  @ApiResponse(responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "403",
      description = "Session does not belong to the authenticated user (code: BUSINESS_RULE_VIOLATION)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "500",
      description = "Internal error (code: OPERATION_FAILED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<RevokeUserSessionResult>> revokeSession(
      @Parameter(description = "Tenant slug", example = "my-company")
      @PathVariable String tenantSlug,
      @Parameter(description = "Session UUID to revoke")
      @PathVariable String sessionId,
      @RequestHeader(value = "Authorization", required = false) String authorization) {

    String bearerToken = extractBearerToken(authorization);

    RevokeUserSessionResult result = revokeUserSessionUseCase.execute(
        new RevokeUserSessionCommand(tenantSlug, bearerToken, sessionId));

    ResponseCode code = result.alreadyClosed()
        ? ResponseCode.ACCOUNT_SESSION_ALREADY_CLOSED
        : ResponseCode.ACCOUNT_SESSION_REVOKED;

    return ResponseEntity.ok(
        BaseResponse.<RevokeUserSessionResult>builder()
            .data(result)
            .success(ResponseHelper.message(code))
            .build());
  }

  /**
   * GET /api/v1/tenants/{tenantSlug}/account/notification-preferences
   *
   * <p>Devuelve las preferencias de notificación del usuario autenticado.
   * Si nunca las configuró, retorna los valores por defecto.
   *
   * @param tenantSlug    slug del tenant
   * @param authorization header Authorization (debe ser "Bearer &lt;token&gt;")
   * @return preferencias actuales o defaults
   */
  @GetMapping("/notification-preferences")
  @Operation(
      summary = "Get notification preferences",
      description = "Returns the notification preferences of the authenticated user. "
                    + "If the user has never configured them, default values are returned. "
                    + "Requires Authorization: Bearer <access_token>.")
  @ApiResponse(responseCode = "200",
      description = "Preferences retrieved (code: ACCOUNT_NOTIFICATION_PREFERENCES_RETRIEVED)")
  @ApiResponse(responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "500",
      description = "Internal error (code: OPERATION_FAILED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<NotificationPreferencesData>> getNotificationPreferences(
      @Parameter(description = "Tenant slug", example = "my-company")
      @PathVariable String tenantSlug,
      @RequestHeader(value = "Authorization", required = false) String authorization) {

    String bearerToken = extractBearerToken(authorization);

    NotificationPreferencesResult result = getNotificationPreferencesUseCase.execute(
        new GetNotificationPreferencesCommand(tenantSlug, bearerToken));

    return ResponseEntity.ok(
        BaseResponse.<NotificationPreferencesData>builder()
            .data(NotificationPreferencesData.from(result))
            .success(ResponseHelper.message(ResponseCode.ACCOUNT_NOTIFICATION_PREFERENCES_RETRIEVED))
            .build());
  }

  /**
   * PATCH /api/v1/tenants/{tenantSlug}/account/notification-preferences
   *
   * <p>Actualiza las preferencias de notificación del usuario autenticado.
   * Operación upsert: crea el registro si no existe. Todos los campos son requeridos.
   * Campos desconocidos en el body retornan 400.
   *
   * @param tenantSlug    slug del tenant
   * @param authorization header Authorization (debe ser "Bearer &lt;token&gt;")
   * @param request       body con los 5 campos boolean
   * @return preferencias resultantes tras la actualización
   */
  @PatchMapping("/notification-preferences")
  @Operation(
      summary = "Update notification preferences",
      description = "Updates all notification preferences of the authenticated user (upsert). "
                    + "All 5 boolean fields are required. Unknown fields in the body return 400. "
                    + "Requires Authorization: Bearer <access_token>.")
  @ApiResponse(responseCode = "200",
      description = "Preferences updated (code: ACCOUNT_NOTIFICATION_PREFERENCES_UPDATED)")
  @ApiResponse(responseCode = "400",
      description = "Validation error — unknown field or invalid body (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "500",
      description = "Internal error (code: OPERATION_FAILED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<NotificationPreferencesData>> updateNotificationPreferences(
      @Parameter(description = "Tenant slug", example = "my-company")
      @PathVariable String tenantSlug,
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @RequestBody UpdateNotificationPreferencesRequest request) {

    String bearerToken = extractBearerToken(authorization);

    NotificationPreferencesResult result = updateNotificationPreferencesUseCase.execute(
        new UpdateNotificationPreferencesCommand(
            tenantSlug, bearerToken,
            request.securityAlertsEmail(),
            request.securityAlertsInApp(),
            request.billingAlertsEmail(),
            request.productUpdatesEmail(),
            request.weeklyDigest()));

    return ResponseEntity.ok(
        BaseResponse.<NotificationPreferencesData>builder()
            .data(NotificationPreferencesData.from(result))
            .success(ResponseHelper.message(ResponseCode.ACCOUNT_NOTIFICATION_PREFERENCES_UPDATED))
            .build());
  }

  /**
   * GET /api/v1/tenants/{tenantSlug}/account/access
   *
   * <p>Devuelve las apps y roles del usuario autenticado en el tenant.
   * No requiere parámetros de admin — es una vista self-service.
   * Retorna lista vacía si el usuario no tiene membresías.
   *
   * @param tenantSlug    slug del tenant
   * @param authorization header Authorization (debe ser "Bearer &lt;token&gt;")
   * @return lista de entradas de acceso (apps + roles del usuario)
   */
  @GetMapping("/access")
  @Operation(
      summary = "Get own access and roles",
      description = "Returns the authenticated user's memberships (apps + roles) within the tenant. "
                    + "Returns an empty list if the user has no memberships. "
                    + "Requires Authorization: Bearer <access_token>.")
  @ApiResponse(responseCode = "200",
      description = "Access retrieved (code: ACCOUNT_ACCESS_RETRIEVED)")
  @ApiResponse(responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "500",
      description = "Internal error (code: OPERATION_FAILED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "503",
      description = "External service unavailable (code: EXTERNAL_SERVICE_ERROR)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<List<UserAccessData>>> getAccess(
      @Parameter(description = "Tenant slug", example = "my-company")
      @PathVariable String tenantSlug,
      @RequestHeader(value = "Authorization", required = false) String authorization) {

    String bearerToken = extractBearerToken(authorization);

    UserAccessResult result = getUserAccessUseCase.execute(
        new GetUserAccessCommand(tenantSlug, bearerToken));

    List<UserAccessData> data = result.entries().stream()
        .map(UserAccessData::from)
        .toList();

    return ResponseEntity.ok(
        BaseResponse.<List<UserAccessData>>builder()
            .data(data)
            .success(ResponseHelper.message(ResponseCode.ACCOUNT_ACCESS_RETRIEVED))
            .build());
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

  private String extractBearerToken(String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      throw new UnauthorizedException(
          "Missing or invalid Authorization header. Expected: Bearer <access_token>");
    }
    return authorization.substring("Bearer ".length()).trim();
  }

  private String extractClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
