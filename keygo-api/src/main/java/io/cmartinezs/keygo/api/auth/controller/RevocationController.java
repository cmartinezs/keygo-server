package io.cmartinezs.keygo.api.auth.controller;

import io.cmartinezs.keygo.api.auth.request.RevokeTokenRequest;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.auth.command.RevokeTokenCommand;
import io.cmartinezs.keygo.app.auth.usecase.RevokeTokenUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller: OAuth 2.0 Token Revocation (RFC 7009).
 *
 * <p>Endpoint:
 * <ul>
 *   <li>POST /api/v1/tenants/{tenantSlug}/oauth2/revoke</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}")
@Tag(name = "OAuth2 / Authorization", description = "OAuth2 Authorization Code + PKCE flow, "
    + "token exchange, refresh token rotation and client_credentials grant — all public endpoints (no X-KEYGO-ADMIN)")
public class RevocationController {

  private final RevokeTokenUseCase revokeTokenUseCase;

  public RevocationController(RevokeTokenUseCase revokeTokenUseCase) {
    this.revokeTokenUseCase = revokeTokenUseCase;
  }

  /**
   * POST /api/v1/tenants/{tenantSlug}/oauth2/revoke
   *
   * <p>Revoca un refresh token. Si el token no existe o ya está revocado,
   * responde 200 igualmente (RFC 7009 §2.2: idempotente y no revelar existencia).
   *
   * @param tenantSlug slug del tenant
   * @param request    parámetros de revocación
   * @return 200 OK siempre (incluso si el token no existe)
   */
  @PostMapping("/oauth2/revoke")
  @Operation(
      summary = "Revoke a token (RFC 7009)",
      description = "Revokes a refresh token per RFC 7009. The endpoint is **idempotent** — "
          + "if the token does not exist or has already been revoked, 200 OK is returned anyway "
          + "to prevent token existence enumeration. No authentication header required.")
  @ApiResponse(responseCode = "200", description = "Token revoked (or silently ignored if unknown)",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  @ApiResponse(responseCode = "400", description = "Invalid request body",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
  public ResponseEntity<BaseResponse<Void>> revoke(
      @Parameter(description = "Tenant slug", example = "my-company") @PathVariable String tenantSlug,
      @Valid @RequestBody RevokeTokenRequest request) {

    revokeTokenUseCase.execute(
        new RevokeTokenCommand(tenantSlug, request.clientId(), request.token(), request.tokenTypeHint()));

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .success(ResponseHelper.message(ResponseCode.TOKEN_REVOKED))
        .build();
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}

