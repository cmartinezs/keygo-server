package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.platform.request.AssignPlatformRoleRequest;
import io.cmartinezs.keygo.api.platform.request.CreatePlatformUserRequest;
import io.cmartinezs.keygo.api.platform.response.PlatformUserData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.membership.command.AssignPlatformRoleCommand;
import io.cmartinezs.keygo.app.membership.usecase.AssignPlatformRoleUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokePlatformRoleUseCase;
import io.cmartinezs.keygo.app.user.command.CreatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.usecase.ActivatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.CreatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetPlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.SuspendPlatformUserUseCase;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for platform-level user management operations.
 * <p>Controlador REST para operaciones de gestión de usuarios globales de la plataforma.
 * All endpoints require KEYGO_ADMIN role.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/platform/users")
@Tag(name = "Platform Users", description = "Platform user management — requires KEYGO_ADMIN")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasRole('KEYGO_ADMIN')")
public class PlatformUserController {

  private final CreatePlatformUserUseCase createPlatformUserUseCase;
  private final GetPlatformUserUseCase getPlatformUserUseCase;
  private final SuspendPlatformUserUseCase suspendPlatformUserUseCase;
  private final ActivatePlatformUserUseCase activatePlatformUserUseCase;
  private final AssignPlatformRoleUseCase assignPlatformRoleUseCase;
  private final RevokePlatformRoleUseCase revokePlatformRoleUseCase;

  public PlatformUserController(
      CreatePlatformUserUseCase createPlatformUserUseCase,
      GetPlatformUserUseCase getPlatformUserUseCase,
      SuspendPlatformUserUseCase suspendPlatformUserUseCase,
      ActivatePlatformUserUseCase activatePlatformUserUseCase,
      AssignPlatformRoleUseCase assignPlatformRoleUseCase,
      RevokePlatformRoleUseCase revokePlatformRoleUseCase) {
    this.createPlatformUserUseCase = createPlatformUserUseCase;
    this.getPlatformUserUseCase = getPlatformUserUseCase;
    this.suspendPlatformUserUseCase = suspendPlatformUserUseCase;
    this.activatePlatformUserUseCase = activatePlatformUserUseCase;
    this.assignPlatformRoleUseCase = assignPlatformRoleUseCase;
    this.revokePlatformRoleUseCase = revokePlatformRoleUseCase;
  }

  /**
   * Create a new global platform user.
   * <p>Crear un nuevo usuario global de la plataforma.
   *
   * @param request the creation request
   * @return 201 Created with the platform user data
   */
  @PostMapping
  @Operation(
      summary = "Create a new platform user",
      description = "Creates a new global platform user with the given email and password. "
                    + "A username is derived from the email prefix. Requires KEYGO_ADMIN role.")
  @ApiResponse(responseCode = "201", description = "Platform user created successfully (code: PLATFORM_USER_CREATED)")
  @ApiResponse(responseCode = "400", description = "Request body validation failed (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "A user with the same email or username already exists (code: DUPLICATE_USER)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<PlatformUserData>> createPlatformUser(
      @Valid @RequestBody CreatePlatformUserRequest request) {

    String username = request.email().split("@")[0];

    PlatformUser user = createPlatformUserUseCase.execute(
        new CreatePlatformUserCommand(
            username,
            request.email(),
            request.password(),
            request.firstName(),
            request.lastName()));

    BaseResponse<PlatformUserData> response = BaseResponse.<PlatformUserData>builder()
        .data(PlatformUserData.from(user))
        .success(ResponseHelper.message(ResponseCode.PLATFORM_USER_CREATED))
        .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Retrieve a platform user by ID.
   * <p>Obtener un usuario de plataforma por su ID.
   *
   * @param userId the platform user UUID
   * @return 200 OK with the platform user data
   */
  @GetMapping("/{userId}")
  @Operation(
      summary = "Get platform user by ID",
      description = "Retrieves platform user details by UUID. Requires KEYGO_ADMIN role.")
  @ApiResponse(responseCode = "200", description = "Platform user retrieved successfully (code: PLATFORM_USER_RETRIEVED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Platform user not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<PlatformUserData>> getPlatformUser(
      @Parameter(description = "UUID of the platform user", example = "550e8400-e29b-41d4-a716-446655440000")
      @PathVariable UUID userId) {

    PlatformUser user = getPlatformUserUseCase.execute(UserId.of(userId));

    BaseResponse<PlatformUserData> response = BaseResponse.<PlatformUserData>builder()
        .data(PlatformUserData.from(user))
        .success(ResponseHelper.message(ResponseCode.PLATFORM_USER_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Suspend a platform user.
   * <p>Suspender un usuario de plataforma.
   *
   * @param userId the platform user UUID
   * @return 200 OK with the suspended platform user data
   */
  @PutMapping("/{userId}/suspend")
  @Operation(
      summary = "Suspend a platform user",
      description = "Suspends an active platform user. A suspended user cannot authenticate. "
                    + "Requires KEYGO_ADMIN role.")
  @ApiResponse(responseCode = "200", description = "Platform user suspended successfully (code: PLATFORM_USER_SUSPENDED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Platform user not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<PlatformUserData>> suspendPlatformUser(
      @Parameter(description = "UUID of the platform user", example = "550e8400-e29b-41d4-a716-446655440000")
      @PathVariable UUID userId) {

    PlatformUser user = suspendPlatformUserUseCase.execute(UserId.of(userId));

    BaseResponse<PlatformUserData> response = BaseResponse.<PlatformUserData>builder()
        .data(PlatformUserData.from(user))
        .success(ResponseHelper.message(ResponseCode.PLATFORM_USER_SUSPENDED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Activate a previously suspended or pending platform user.
   * <p>Activar un usuario de plataforma previamente suspendido o pendiente.
   *
   * @param userId the platform user UUID
   * @return 200 OK with the activated platform user data
   */
  @PutMapping("/{userId}/activate")
  @Operation(
      summary = "Activate a platform user",
      description = "Reactivates a suspended or pending platform user. Requires KEYGO_ADMIN role.")
  @ApiResponse(responseCode = "200", description = "Platform user activated successfully (code: PLATFORM_USER_ACTIVATED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Platform user not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<PlatformUserData>> activatePlatformUser(
      @Parameter(description = "UUID of the platform user", example = "550e8400-e29b-41d4-a716-446655440000")
      @PathVariable UUID userId) {

    PlatformUser user = activatePlatformUserUseCase.execute(UserId.of(userId));

    BaseResponse<PlatformUserData> response = BaseResponse.<PlatformUserData>builder()
        .data(PlatformUserData.from(user))
        .success(ResponseHelper.message(ResponseCode.PLATFORM_USER_ACTIVATED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Assign a platform role to a user.
   * <p>Asignar un rol de plataforma a un usuario.
   *
   * @param userId  the platform user UUID
   * @param request the role assignment request
   * @return 200 OK on successful assignment
   */
  @PostMapping("/{userId}/platform-roles")
  @Operation(
      summary = "Assign a platform role",
      description = "Assigns a platform role to a global user. Idempotent — re-assigning "
                    + "an existing role returns the existing assignment. Requires KEYGO_ADMIN role.")
  @ApiResponse(responseCode = "200", description = "Platform role assigned successfully (code: PLATFORM_ROLE_ASSIGNED)")
  @ApiResponse(responseCode = "400", description = "Invalid request (code: INVALID_INPUT)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Platform role not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<Void>> assignPlatformRole(
      @Parameter(description = "UUID of the platform user", example = "550e8400-e29b-41d4-a716-446655440000")
      @PathVariable UUID userId,
      @Valid @RequestBody AssignPlatformRoleRequest request) {

    assignPlatformRoleUseCase.execute(
        new AssignPlatformRoleCommand(userId, request.roleCode()));

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .success(ResponseHelper.message(ResponseCode.PLATFORM_ROLE_ASSIGNED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * Revoke a platform role from a user.
   * <p>Revocar un rol de plataforma de un usuario.
   *
   * @param userId   the platform user UUID
   * @param roleCode the platform role code to revoke
   * @return 200 OK on successful revocation
   */
  @DeleteMapping("/{userId}/platform-roles/{roleCode}")
  @Operation(
      summary = "Revoke a platform role",
      description = "Revokes a platform role from a global user. Idempotent — revoking a role "
                    + "not assigned is a no-op. Requires KEYGO_ADMIN role.")
  @ApiResponse(responseCode = "200", description = "Platform role revoked successfully (code: PLATFORM_ROLE_REVOKED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Platform role not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<Void>> revokePlatformRole(
      @Parameter(description = "UUID of the platform user", example = "550e8400-e29b-41d4-a716-446655440000")
      @PathVariable UUID userId,
      @Parameter(description = "Code of the platform role to revoke", example = "keygo_admin")
      @PathVariable String roleCode) {

    revokePlatformRoleUseCase.execute(userId, roleCode);

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .success(ResponseHelper.message(ResponseCode.PLATFORM_ROLE_REVOKED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
