package io.cmartinezs.keygo.api.auth.controller;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.auth.command.GetUserInfoCommand;
import io.cmartinezs.keygo.app.auth.result.UserInfoResult;
import io.cmartinezs.keygo.app.auth.usecase.GetUserInfoUseCase;
import io.cmartinezs.keygo.api.error.UnauthorizedException;
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
 * Controller: OIDC UserInfo endpoint (OIDC Core 1.0 §5.3).
 *
 * <p>Endpoint:
 * <ul>
 *   <li>GET /api/v1/tenants/{tenantSlug}/userinfo</li>
 * </ul>
 *
 * <p>Requiere {@code Authorization: Bearer <access_token>}.
 * No requiere {@code X-KEYGO-ADMIN} (endpoint de usuario, no de administración).
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}")
@Tag(name = "OIDC Discovery", description = "Public OIDC discovery and JWKS endpoints — no authentication required. "
    + "Returns raw JSON (not BaseResponse) for compatibility with third-party OAuth2/OIDC libraries.")
public class UserInfoController {

  private final GetUserInfoUseCase getUserInfoUseCase;

  public UserInfoController(GetUserInfoUseCase getUserInfoUseCase) {
    this.getUserInfoUseCase = getUserInfoUseCase;
  }

  /**
   * GET /api/v1/tenants/{tenantSlug}/userinfo
   *
   * <p>Retorna claims de identidad del usuario autenticado según OIDC §5.3.
   *
   * @param tenantSlug    slug del tenant
   * @param authorization header Authorization (debe ser "Bearer &lt;token&gt;")
   * @return claims del usuario autenticado
   */
  @GetMapping("/userinfo")
  @Operation(
      summary = "Get authenticated user info (OIDC §5.3)",
      description = "Returns identity claims of the authenticated user per OIDC Core 1.0 §5.3. "
          + "Requires `Authorization: Bearer <access_token>` — the token must have been issued by "
          + "the `POST /oauth2/token` endpoint with `openid` scope.")
  @ApiResponse(responseCode = "200", description = "User info claims retrieved",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "404", description = "User or tenant not found",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<UserInfoResult>> userInfo(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug,
      @Parameter(description = "Bearer access token", example = "Bearer eyJhbGci...")
      @RequestHeader(value = "Authorization", required = false) String authorization) {

    if (authorization == null || !authorization.startsWith("Bearer ")) {
      throw new UnauthorizedException("Missing or invalid Authorization header. Expected: Bearer <token>");
    }

    String bearerToken = authorization.substring("Bearer ".length()).trim();

    UserInfoResult result = getUserInfoUseCase.execute(new GetUserInfoCommand(tenantSlug, bearerToken));

    BaseResponse<UserInfoResult> response = BaseResponse.<UserInfoResult>builder()
        .data(result)
        .success(ResponseHelper.message(ResponseCode.USER_INFO_RETRIEVED))
        .build();
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}

