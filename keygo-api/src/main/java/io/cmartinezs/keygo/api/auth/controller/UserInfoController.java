package io.cmartinezs.keygo.api.auth.controller;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.auth.command.GetUserInfoCommand;
import io.cmartinezs.keygo.app.auth.result.UserInfoResult;
import io.cmartinezs.keygo.app.auth.usecase.GetUserInfoUseCase;
import io.cmartinezs.keygo.api.error.UnauthorizedException;
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
  public ResponseEntity<BaseResponse<UserInfoResult>> userInfo(
      @PathVariable String tenantSlug,
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

