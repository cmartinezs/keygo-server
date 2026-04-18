package io.cmartinezs.keygo.api.registration.controller;

import io.cmartinezs.keygo.api.registration.request.RegisterRequest;
import io.cmartinezs.keygo.api.registration.request.ResendVerificationRequest;
import io.cmartinezs.keygo.api.registration.request.VerifyEmailRequest;
import io.cmartinezs.keygo.api.registration.response.RegistrationData;
import io.cmartinezs.keygo.api.registration.response.RegistrationInfoData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.response.NotificationSentData;
import io.cmartinezs.keygo.app.user.command.RegisterTenantUserCommand;
import io.cmartinezs.keygo.app.user.command.ResendVerificationCommand;
import io.cmartinezs.keygo.app.user.command.VerifyEmailCommand;
import io.cmartinezs.keygo.app.user.orchestrator.SelfRegistrationOrchestrator;
import io.cmartinezs.keygo.app.user.usecase.RegisterTenantUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResendVerificationEmailUseCase;
import io.cmartinezs.keygo.domain.shared.util.EmailMasker;
import io.cmartinezs.keygo.domain.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST controller for user self-registration within a tenant's client app.
 * <p>Controlador REST público para auto-registro de usuarios en la app de un tenant.
 * All endpoints are PUBLIC — no X-KEYGO-ADMIN header is required.
 * <p>Todos los endpoints son PÚBLICOS — no se requiere el header X-KEYGO-ADMIN.
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/apps/{clientId}")
@Tag(name = "Registration", description = "Public user self-registration and email verification endpoints")
public class RegistrationController {

  private final RegisterTenantUserUseCase registerTenantUserUseCase;
  private final SelfRegistrationOrchestrator selfRegistrationOrchestrator;
  private final ResendVerificationEmailUseCase resendVerificationEmailUseCase;

  public RegistrationController(
      RegisterTenantUserUseCase registerTenantUserUseCase,
      SelfRegistrationOrchestrator selfRegistrationOrchestrator,
      ResendVerificationEmailUseCase resendVerificationEmailUseCase) {
    this.registerTenantUserUseCase = registerTenantUserUseCase;
    this.selfRegistrationOrchestrator = selfRegistrationOrchestrator;
    this.resendVerificationEmailUseCase = resendVerificationEmailUseCase;
  }

  /**
   * Register a new user in the given tenant's client app.
   * <p>Registra un nuevo usuario en la app del tenant dado.
   * The user is created with PENDING status and a verification email is sent.
   */
  @PostMapping("/register")
  @Operation(
      summary = "Register a new user",
      description = "Creates a new user with PENDING status and sends a 6-digit verification "
                    + "code to the provided email. The code is valid for 30 minutes.")
  @ApiResponse(responseCode = "201", description = "User registered — verification email sent (code: USER_REGISTERED)")
  @ApiResponse(responseCode = "400", description = "Request body validation failed (code: INVALID_INPUT). data.field_errors lists each invalid field.",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant or client app not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "403", description = "App does not allow self-registration — INVITE_ONLY policy (code: BUSINESS_RULE_VIOLATION)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "Email or username already exists in this tenant (code: DUPLICATE_RESOURCE)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<RegistrationData>> register(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app ID") @PathVariable String clientId,
      @Valid @RequestBody RegisterRequest request) {

    RegisterTenantUserCommand command = new RegisterTenantUserCommand(
        tenantSlug, clientId,
        request.username(), request.email(), request.password(),
        request.firstName(), request.lastName());

    User user = registerTenantUserUseCase.execute(command);

    RegistrationData data = new RegistrationData(
        user.getId().value(),
        user.getUsername().value(),
        EmailMasker.mask(user.getEmail().value()),
        user.getStatus());

    BaseResponse<RegistrationData> response = BaseResponse.<RegistrationData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.USER_REGISTERED))
        .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Verify the user's email using the code sent during registration.
   * <p>Verifica el email del usuario usando el código enviado durante el registro.
   */
  @PostMapping("/verify-email")
  @Operation(
      summary = "Verify email address",
      description = "Validates the 6-digit code received by email. If valid, the user is "
                    + "activated (status → ACTIVE) and automatic membership is created based on "
                    + "the app's registration policy. If expired, a new code must be requested.")
  @ApiResponse(responseCode = "200", description = "Email verified — user is now active (code: EMAIL_VERIFIED)")
  @ApiResponse(responseCode = "400", description = "Invalid or already-used verification code (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "User or client app not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "422", description = "Verification code has expired — request a new one (code: EMAIL_VERIFICATION_EXPIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<Void>> verifyEmail(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app ID") @PathVariable String clientId,
      @Valid @RequestBody VerifyEmailRequest request) {

    selfRegistrationOrchestrator.execute(tenantSlug, clientId, request.email(), request.registration_id(), request.code());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .success(ResponseHelper.message(ResponseCode.EMAIL_VERIFIED))
        .build();

    return ResponseEntity.ok(response);
  }

  /**
   * Resend a new verification email (only allowed if the previous code has expired).
   * <p>Reenvía un nuevo email de verificación (solo si el código anterior ya venció).
   */
  @PostMapping("/resend-verification")
  @Operation(
      summary = "Resend verification email",
      description = "Generates and sends a new verification code. Only allowed once the "
                    + "previous code has expired (30-minute window).")
  @ApiResponse(responseCode = "200", description = "New verification email sent (code: EMAIL_VERIFICATION_RESENT)")
  @ApiResponse(responseCode = "404", description = "User not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "Current code is still active — wait until it expires (code: BUSINESS_RULE_VIOLATION)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<NotificationSentData>> resendVerification(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Client app ID") @PathVariable String clientId,
      @Valid @RequestBody ResendVerificationRequest request) {

    ResendVerificationCommand command = new ResendVerificationCommand(
        tenantSlug, clientId, request.email());

    resendVerificationEmailUseCase.execute(command);

    var data = new NotificationSentData(EmailMasker.mask(request.email()));

    BaseResponse<NotificationSentData> response = BaseResponse.<NotificationSentData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.EMAIL_VERIFICATION_RESENT))
        .build();

    return ResponseEntity.ok(response);
  }
}

