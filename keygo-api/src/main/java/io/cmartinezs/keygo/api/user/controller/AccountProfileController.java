package io.cmartinezs.keygo.api.user.controller;

import io.cmartinezs.keygo.api.error.UnauthorizedException;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.user.request.UpdateUserProfileRequest;
import io.cmartinezs.keygo.api.user.response.UserProfileData;
import io.cmartinezs.keygo.app.user.command.GetUserProfileCommand;
import io.cmartinezs.keygo.app.user.command.UpdateUserProfileCommand;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
import io.cmartinezs.keygo.app.user.usecase.GetUserProfileUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdateUserProfileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller: self-service de perfil de usuario autenticado.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /api/v1/tenants/{tenantSlug}/account/profile — consultar propio perfil</li>
 *   <li>PATCH /api/v1/tenants/{tenantSlug}/account/profile — actualizar propio perfil</li>
 * </ul>
 *
 * <p>Ambos endpoints requieren {@code Authorization: Bearer <access_token>}.
 * No requieren {@code X-KEYGO-ADMIN} — son endpoints de usuario final, no de administración.
 * El filtro {@code BootstrapAdminKeyFilter} los declara públicos vía sufijo {@code /account/profile}.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/account/profile")
@Tag(name = "Account Profile", description = "Self-service profile management — requires Bearer token (no X-KEYGO-ADMIN)")
public class AccountProfileController {

  private final GetUserProfileUseCase getUserProfileUseCase;
  private final UpdateUserProfileUseCase updateUserProfileUseCase;

  public AccountProfileController(
      GetUserProfileUseCase getUserProfileUseCase,
      UpdateUserProfileUseCase updateUserProfileUseCase) {
    this.getUserProfileUseCase = getUserProfileUseCase;
    this.updateUserProfileUseCase = updateUserProfileUseCase;
  }

  /**
   * GET /api/v1/tenants/{tenantSlug}/account/profile
   *
   * <p>Retorna el perfil completo del usuario autenticado (todos los campos OIDC extendidos).
   *
   * @param tenantSlug    slug del tenant
   * @param authorization header Authorization (debe ser "Bearer &lt;token&gt;")
   * @return perfil completo del usuario
   */
  @GetMapping
  @Operation(
      summary = "Get own profile",
      description = "Returns the complete profile of the authenticated user (all OIDC extended fields). "
                    + "Requires Authorization: Bearer <access_token>.")
  @ApiResponse(responseCode = "200", description = "Profile retrieved successfully (code: USER_PROFILE_RETRIEVED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "User or tenant not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<UserProfileData>> getProfile(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug,
      @RequestHeader(value = "Authorization", required = false) String authorization) {

    String bearerToken = extractBearerToken(authorization);

    UserProfileResult result = getUserProfileUseCase.execute(
        new GetUserProfileCommand(tenantSlug, bearerToken));

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<UserProfileData>builder()
            .data(toData(result))
            .success(ResponseHelper.message(ResponseCode.USER_PROFILE_RETRIEVED))
            .build());
  }

  /**
   * PATCH /api/v1/tenants/{tenantSlug}/account/profile
   *
   * <p>Actualiza parcialmente el perfil del usuario autenticado.
   * Solo se actualizan los campos enviados (no-nulos) — semántica PATCH.
   *
   * @param tenantSlug    slug del tenant
   * @param authorization header Authorization (debe ser "Bearer &lt;token&gt;")
   * @param request       campos de perfil a actualizar (todos opcionales)
   * @return perfil actualizado del usuario
   */
  @PatchMapping
  @Operation(
      summary = "Update own profile",
      description = "Partially updates the profile of the authenticated user. "
                    + "Only non-null fields are updated (PATCH semantics). "
                    + "Requires Authorization: Bearer <access_token>.")
  @ApiResponse(responseCode = "200", description = "Profile updated successfully (code: USER_PROFILE_UPDATED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "User or tenant not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<UserProfileData>> updateProfile(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug,
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @RequestBody UpdateUserProfileRequest request) {

    String bearerToken = extractBearerToken(authorization);

    UserProfileResult result = updateUserProfileUseCase.execute(
        new UpdateUserProfileCommand(
            tenantSlug, bearerToken,
            request.firstName(), request.lastName(),
            request.phoneNumber(), request.locale(), request.zoneinfo(),
            request.profilePictureUrl(), request.birthdate(), request.website()));

    return ResponseEntity.status(HttpStatus.OK).body(
        BaseResponse.<UserProfileData>builder()
            .data(toData(result))
            .success(ResponseHelper.message(ResponseCode.USER_PROFILE_UPDATED))
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
