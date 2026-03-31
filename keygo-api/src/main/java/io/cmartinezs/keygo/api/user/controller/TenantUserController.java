package io.cmartinezs.keygo.api.user.controller;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.user.request.CreateUserRequest;
import io.cmartinezs.keygo.api.user.request.ResetPasswordRequest;
import io.cmartinezs.keygo.api.user.request.UpdateUserRequest;
import io.cmartinezs.keygo.api.user.request.ValidateCredentialsRequest;
import io.cmartinezs.keygo.api.user.response.UserData;
import io.cmartinezs.keygo.app.user.command.CreateUserCommand;
import io.cmartinezs.keygo.app.user.command.ResetUserPasswordCommand;
import io.cmartinezs.keygo.app.user.command.UpdateUserCommand;
import io.cmartinezs.keygo.app.user.usecase.CreateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListUsersUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResetUserPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ValidateUserCredentialsUseCase;
import io.cmartinezs.keygo.domain.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing users within a tenant.
 * <p>Controlador REST para gestión de usuarios dentro de un tenant.
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/users")
@Tag(name = "Users", description = "User identity management per tenant — requires Bearer JWT")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasAnyRole('ADMIN','ADMIN_TENANT') and @tenantAuthorizationEvaluator.hasTenantAccess(authentication)")
public class TenantUserController {

  private final CreateUserUseCase createUserUseCase;
  private final ListUsersUseCase listUsersUseCase;
  private final GetUserUseCase getUserUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final ResetUserPasswordUseCase resetUserPasswordUseCase;
  private final ValidateUserCredentialsUseCase validateUserCredentialsUseCase;

  public TenantUserController(
      CreateUserUseCase createUserUseCase,
      ListUsersUseCase listUsersUseCase,
      GetUserUseCase getUserUseCase,
      UpdateUserUseCase updateUserUseCase,
      ResetUserPasswordUseCase resetUserPasswordUseCase,
      ValidateUserCredentialsUseCase validateUserCredentialsUseCase) {
    this.createUserUseCase = createUserUseCase;
    this.listUsersUseCase = listUsersUseCase;
    this.getUserUseCase = getUserUseCase;
    this.updateUserUseCase = updateUserUseCase;
    this.resetUserPasswordUseCase = resetUserPasswordUseCase;
    this.validateUserCredentialsUseCase = validateUserCredentialsUseCase;
  }

  /**
   * Create a new user within the given tenant.
   * <p>Crea un nuevo usuario dentro del tenant dado.
   */
  @PostMapping
  @Operation(
      summary = "Create a user",
      description = "Creates a new user account scoped to the specified tenant. "
                    + "Username and email must be unique within the tenant.")
  @ApiResponse(responseCode = "201", description = "User created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request body",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid admin key",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "Email or username already exists in this tenant",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<UserData>> createUser(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug,
      @Valid @RequestBody CreateUserRequest request) {

    User user = createUserUseCase.execute(new CreateUserCommand(
        tenantSlug,
        request.username(),
        request.email(),
        request.password(),
        request.firstName(),
        request.lastName()));

    return ResponseEntity.status(HttpStatus.CREATED).body(
        BaseResponse.<UserData>builder()
            .data(toData(user))
            .success(ResponseHelper.message(ResponseCode.USER_CREATED))
            .build());
  }

  /**
   * List all users of the given tenant.
   * <p>Lista todos los usuarios del tenant dado.
   */
  @GetMapping
  @Operation(
      summary = "List users",
      description = "Returns all user accounts registered under the specified tenant.")
  @ApiResponse(responseCode = "200", description = "User list retrieved successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid admin key",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<List<UserData>>> listUsers(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug) {

    List<UserData> data = listUsersUseCase.execute(tenantSlug).stream()
        .map(this::toData)
        .toList();

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<List<UserData>>builder()
            .data(data)
            .success(ResponseHelper.message(ResponseCode.USER_LIST_RETRIEVED))
            .build());
  }

  /**
   * Retrieve a specific user by its UUID.
   * <p>Obtiene un usuario específico por su UUID.
   */
  @GetMapping("/{userId}")
  @Operation(
      summary = "Get a user",
      description = "Retrieves details of a specific user by its UUID within the tenant.")
  @ApiResponse(responseCode = "200", description = "User retrieved successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid admin key",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "User or tenant not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<UserData>> getUser(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug,
      @Parameter(description = "User UUID", example = "a1b2c3d4-e5f6-...") @PathVariable String userId) {

    User user = getUserUseCase.execute(tenantSlug, userId);

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<UserData>builder()
            .data(toData(user))
            .success(ResponseHelper.message(ResponseCode.USER_RETRIEVED))
            .build());
  }

  /**
   * Update user profile information.
   * <p>Actualiza la información de perfil del usuario.
   */
  @PutMapping("/{userId}")
  @Operation(
      summary = "Update a user",
      description = "Updates the firstName and lastName of an existing user.")
  @ApiResponse(responseCode = "200", description = "User updated successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid admin key",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "User or tenant not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<UserData>> updateUser(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug,
      @Parameter(description = "User UUID", example = "a1b2c3d4-e5f6-...") @PathVariable String userId,
      @RequestBody UpdateUserRequest request) {

    User user = updateUserUseCase.execute(new UpdateUserCommand(
        tenantSlug, userId,
        request.firstName(), request.lastName(),
        request.phoneNumber(), request.locale(), request.zoneinfo(),
        request.profilePictureUrl(), request.birthdate(), request.website()));

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<UserData>builder()
            .data(toData(user))
            .success(ResponseHelper.message(ResponseCode.USER_UPDATED))
            .build());
  }

  /**
   * Reset a user's password (admin-initiated).
   * <p>Resetea la contraseña de un usuario (iniciado por el administrador).
   */
  @PostMapping("/{userId}/reset-password")
  @Operation(
      summary = "Reset user password",
      description = "Administratively resets the password of a user within the tenant.")
  @ApiResponse(responseCode = "200", description = "Password reset successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request body",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid admin key",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "User or tenant not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<UserData>> resetPassword(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug,
      @Parameter(description = "User UUID", example = "a1b2c3d4-e5f6-...") @PathVariable String userId,
      @Valid @RequestBody ResetPasswordRequest request) {

    User user = resetUserPasswordUseCase.execute(
        new ResetUserPasswordCommand(tenantSlug, userId, request.newPassword()));

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<UserData>builder()
            .data(toData(user))
            .success(ResponseHelper.message(ResponseCode.USER_PASSWORD_RESET))
            .build());
  }

  /**
   * Validate user credentials (email or username + password).
   * <p>Valida las credenciales de un usuario (email o username + contraseña).
   */
  @PostMapping("/validate-credentials")
  @Operation(
      summary = "Validate credentials",
      description = "Validates a user's credentials (email or username + password) within the tenant. "
                    + "Returns user data on success.")
  @ApiResponse(responseCode = "200", description = "Credentials valid")
  @ApiResponse(responseCode = "400", description = "Invalid request body",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Invalid credentials or admin key missing",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "403", description = "User account is suspended",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "User or tenant not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<UserData>> validateCredentials(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug,
      @Valid @RequestBody ValidateCredentialsRequest request) {

    User user = validateUserCredentialsUseCase.execute(
        tenantSlug, request.credential(), request.password());

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<UserData>builder()
            .data(toData(user))
            .success(ResponseHelper.message(ResponseCode.CREDENTIALS_VALID))
            .build());
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

  private UserData toData(User user) {
    return UserData.builder()
        .id(user.getId().toString())
        .tenantId(user.getTenantId().toString())
        .username(user.getUsername().value())
        .email(user.getEmail().value())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .status(user.getStatus().name())
        .phoneNumber(user.getPhoneNumber())
        .locale(user.getLocale())
        .zoneinfo(user.getZoneinfo())
        .profilePictureUrl(user.getProfilePictureUrl())
        .birthdate(user.getBirthdate())
        .website(user.getWebsite())
        .build();
  }
}

