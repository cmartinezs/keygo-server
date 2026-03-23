package io.cmartinezs.keygo.api.auth.controller;

import io.cmartinezs.keygo.api.auth.request.RevokeTokenRequest;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.auth.command.RevokeTokenCommand;
import io.cmartinezs.keygo.app.auth.usecase.RevokeTokenUseCase;
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
  public ResponseEntity<BaseResponse<Void>> revoke(
      @PathVariable String tenantSlug,
      @Valid @RequestBody RevokeTokenRequest request) {

    revokeTokenUseCase.execute(
        new RevokeTokenCommand(tenantSlug, request.clientId(), request.token(), request.tokenTypeHint()));

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .success(ResponseHelper.message(ResponseCode.TOKEN_REVOKED))
        .build();
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}

