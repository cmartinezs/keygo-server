package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.platform.request.PlatformLoginRequest;
import io.cmartinezs.keygo.api.platform.request.PlatformTokenRequest;
import io.cmartinezs.keygo.api.platform.response.PlatformTokenData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.platform.command.RotatePlatformRefreshTokenCommand;
import io.cmartinezs.keygo.app.platform.result.IssuePlatformTokensResult;
import io.cmartinezs.keygo.app.platform.usecase.IssuePlatformTokensUseCase;
import io.cmartinezs.keygo.app.platform.usecase.RotatePlatformRefreshTokenUseCase;
import io.cmartinezs.keygo.app.user.command.AuthenticatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.usecase.AuthenticatePlatformUserUseCase;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para autenticación de plataforma (login directo + rotación de refresh token).
 *
 * <p>Endpoints públicos — no requieren Bearer token.
 * La seguridad se gestiona validando credenciales directamente.
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/platform")
@Tag(name = "Platform Auth", description = "Platform authentication endpoints")
public class PlatformAuthController {

  private final AuthenticatePlatformUserUseCase authenticateUseCase;
  private final IssuePlatformTokensUseCase issueTokensUseCase;
  private final RotatePlatformRefreshTokenUseCase rotateRefreshTokenUseCase;

  public PlatformAuthController(
      AuthenticatePlatformUserUseCase authenticateUseCase,
      IssuePlatformTokensUseCase issueTokensUseCase,
      RotatePlatformRefreshTokenUseCase rotateRefreshTokenUseCase) {
    this.authenticateUseCase = authenticateUseCase;
    this.issueTokensUseCase = issueTokensUseCase;
    this.rotateRefreshTokenUseCase = rotateRefreshTokenUseCase;
  }

  @PostMapping("/account/login")
  @Operation(summary = "Platform login", description = "Authenticate platform user and issue tokens")
  public ResponseEntity<BaseResponse<PlatformTokenData>> login(
      @RequestBody PlatformLoginRequest request,
      HttpServletRequest httpRequest) {

    // 1. Authenticate via use case
    var command = new AuthenticatePlatformUserCommand(request.email(), request.password());
    PlatformUser platformUser = authenticateUseCase.execute(command);

    // 2. Issue tokens
    String issuer = buildIssuer(httpRequest);
    IssuePlatformTokensResult result = issueTokensUseCase.execute(
        platformUser, issuer,
        httpRequest.getHeader("User-Agent"),
        extractIpAddress(httpRequest));

    // 3. Build response
    var data = new PlatformTokenData(
        result.accessToken(), result.refreshToken(),
        result.tokenType(), result.expiresIn());

    return ResponseEntity.ok(BaseResponse.<PlatformTokenData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_LOGIN_SUCCESS))
        .build());
  }

  @PostMapping("/oauth2/token")
  @Operation(summary = "Platform token rotation", description = "Rotate platform refresh token")
  public ResponseEntity<BaseResponse<PlatformTokenData>> token(
      @RequestBody PlatformTokenRequest request) {

    if (!"refresh_token".equals(request.grantType())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(BaseResponse.<PlatformTokenData>builder()
              .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT,
                  "Only grant_type=refresh_token is supported"))
              .build());
    }

    var command = new RotatePlatformRefreshTokenCommand(request.refreshToken());
    IssuePlatformTokensResult result = rotateRefreshTokenUseCase.execute(command);

    var data = new PlatformTokenData(
        result.accessToken(), result.refreshToken(),
        result.tokenType(), result.expiresIn());

    return ResponseEntity.ok(BaseResponse.<PlatformTokenData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_TOKEN_ROTATED))
        .build());
  }

  private String buildIssuer(HttpServletRequest request) {
    int port = request.getServerPort();
    String portSuffix = (port == 80 || port == 443) ? "" : ":" + port;
    return request.getScheme() + "://" + request.getServerName() + portSuffix
        + request.getContextPath() + "/api/v1/platform";
  }

  private String extractIpAddress(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
  }
}
