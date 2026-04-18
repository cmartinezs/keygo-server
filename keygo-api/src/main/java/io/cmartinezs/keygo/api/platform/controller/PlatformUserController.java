package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.platform.request.AssignPlatformRoleRequest;
import io.cmartinezs.keygo.api.platform.request.CreatePlatformUserRequest;
import io.cmartinezs.keygo.api.platform.response.PlatformUserData;
import io.cmartinezs.keygo.api.platform.response.PlatformUserRoleData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.PagedData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.membership.result.PlatformUserRoleResult;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.membership.command.AssignPlatformRoleCommand;
import io.cmartinezs.keygo.app.membership.usecase.AssignPlatformRoleUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListPlatformUserRolesUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokePlatformRoleUseCase;
import io.cmartinezs.keygo.app.user.filter.PlatformUserFilter;
import io.cmartinezs.keygo.app.user.command.CreatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.usecase.ActivatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.CreatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetPlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.ListPlatformUsersUseCase;
import io.cmartinezs.keygo.app.user.usecase.SuspendPlatformUserUseCase;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
  private final ListPlatformUsersUseCase listPlatformUsersUseCase;
  private final GetPlatformUserUseCase getPlatformUserUseCase;
  private final SuspendPlatformUserUseCase suspendPlatformUserUseCase;
  private final ActivatePlatformUserUseCase activatePlatformUserUseCase;
  private final ListPlatformUserRolesUseCase listPlatformUserRolesUseCase;
  private final AssignPlatformRoleUseCase assignPlatformRoleUseCase;
  private final RevokePlatformRoleUseCase revokePlatformRoleUseCase;

  public PlatformUserController(
      CreatePlatformUserUseCase createPlatformUserUseCase,
      ListPlatformUsersUseCase listPlatformUsersUseCase,
      GetPlatformUserUseCase getPlatformUserUseCase,
      SuspendPlatformUserUseCase suspendPlatformUserUseCase,
      ActivatePlatformUserUseCase activatePlatformUserUseCase,
      ListPlatformUserRolesUseCase listPlatformUserRolesUseCase,
      AssignPlatformRoleUseCase assignPlatformRoleUseCase,
      RevokePlatformRoleUseCase revokePlatformRoleUseCase) {
    this.createPlatformUserUseCase = createPlatformUserUseCase;
    this.listPlatformUsersUseCase = listPlatformUsersUseCase;
    this.getPlatformUserUseCase = getPlatformUserUseCase;
    this.suspendPlatformUserUseCase = suspendPlatformUserUseCase;
    this.activatePlatformUserUseCase = activatePlatformUserUseCase;
    this.listPlatformUserRolesUseCase = listPlatformUserRolesUseCase;
    this.assignPlatformRoleUseCase = assignPlatformRoleUseCase;
    this.revokePlatformRoleUseCase = revokePlatformRoleUseCase;
  }

  /**
   * List platform users with optional pagination, filtering, and sorting.
   * <p>Lista usuarios de plataforma con paginacion, filtrado y ordenamiento opcionales.
   */
  @GetMapping
  @Operation(
      summary = "List platform users",
      description = "Returns a paginated list of global platform users. Supports filtering by "
          + "status and partial username or email match, plus sorting. Requires KEYGO_ADMIN role.")
  @ApiResponse(
      responseCode = "200",
      description = "Platform user list retrieved successfully (code: PLATFORM_USER_LIST_RETRIEVED)")
  @ApiResponse(
      responseCode = "400",
      description =
          "Invalid pagination parameters (code: INVALID_INPUT). data.field_errors lists each invalid field.",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(
      responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<PagedData<PlatformUserData>>> listPlatformUsers(
      @Parameter(description = "Filter by platform user status (ACTIVE, SUSPENDED, PENDING, RESET_PASSWORD)")
      @RequestParam(required = false) UserStatus status,
      @Parameter(description = "Partial match on username (case-insensitive)")
      @RequestParam(name = "username_like", required = false) String usernameLike,
      @Parameter(description = "Partial match on email (case-insensitive)")
      @RequestParam(name = "email_like", required = false) String emailLike,
      @Parameter(description = "Zero-based page number", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size (1–200)", example = "20")
      @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Sort field (username, email, status, createdAt, firstName, lastName)")
      @RequestParam(required = false) String sort,
      @Parameter(description = "Sort order (ASC, DESC)", example = "ASC")
      @RequestParam(required = false) String order) {

    PlatformUserFilter filter =
        PlatformUserFilter.of(status, usernameLike, emailLike, page, size, sort, order);
    PagedResult<PlatformUser> result = listPlatformUsersUseCase.execute(filter);

    List<PlatformUserData> content = result.getContent().stream().map(PlatformUserData::from).toList();

    PagedData<PlatformUserData> pagedData =
        PagedData.<PlatformUserData>builder()
            .content(content)
            .page(result.getPage())
            .size(result.getSize())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .last(result.isLast())
            .build();

    BaseResponse<PagedData<PlatformUserData>> response =
        BaseResponse.<PagedData<PlatformUserData>>builder()
            .data(pagedData)
            .success(ResponseHelper.message(ResponseCode.PLATFORM_USER_LIST_RETRIEVED))
            .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
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
   * List platform roles assigned to a platform user.
   * <p>Lista roles de plataforma asignados a un usuario global.
   */
  @GetMapping("/{userId}/platform-roles")
  @Operation(
      summary = "List platform roles for a user",
      description = "Retrieves the platform roles currently assigned to a global user. "
          + "Requires KEYGO_ADMIN role.")
  @ApiResponse(
      responseCode = "200",
      description = "Platform role list retrieved successfully (code: PLATFORM_ROLE_LIST_RETRIEVED)")
  @ApiResponse(
      responseCode = "401",
      description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Platform user not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<List<PlatformUserRoleData>>> listPlatformUserRoles(
      @Parameter(description = "UUID of the platform user", example = "550e8400-e29b-41d4-a716-446655440000")
      @PathVariable UUID userId) {

    List<PlatformUserRoleResult> roles = listPlatformUserRolesUseCase.execute(userId);
    List<PlatformUserRoleData> data = roles.stream().map(PlatformUserRoleData::from).toList();

    BaseResponse<List<PlatformUserRoleData>> response = BaseResponse.<List<PlatformUserRoleData>>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_ROLE_LIST_RETRIEVED))
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
        new AssignPlatformRoleCommand(userId, request.roleCodes()));

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
