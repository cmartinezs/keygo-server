package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.platform.request.PlatformLoginRequest;
import io.cmartinezs.keygo.api.platform.request.PlatformTokenRequest;
import io.cmartinezs.keygo.api.platform.response.PlatformAuthorizationData;
import io.cmartinezs.keygo.api.platform.response.PlatformLoginData;
import io.cmartinezs.keygo.api.platform.response.PlatformTokenData;
import io.cmartinezs.keygo.api.platform.session.PlatformAuthCodeState;
import io.cmartinezs.keygo.api.platform.session.PlatformAuthorizationSessionState;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.platform.command.RotatePlatformRefreshTokenCommand;
import io.cmartinezs.keygo.app.platform.result.IssuePlatformTokensResult;
import io.cmartinezs.keygo.app.platform.usecase.IssuePlatformTokensUseCase;
import io.cmartinezs.keygo.app.platform.usecase.RotatePlatformRefreshTokenUseCase;
import io.cmartinezs.keygo.app.user.command.AuthenticatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.usecase.AuthenticatePlatformUserUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetPlatformUserUseCase;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller REST para autenticación de plataforma (PKCE + login directo + rotación de refresh token).
 *
 * <p>Endpoints públicos — no requieren Bearer token.
 * La seguridad se gestiona validando credenciales y PKCE directamente.
 *
 * <p>Flujo PKCE:
 * <ol>
 *   <li>GET /oauth2/authorize — valida redirect_uri y code_challenge, almacena en sesión
 *   <li>POST /account/login — autentica, genera authorization code, retorna code + redirect_uri
 *   <li>POST /oauth2/token (grant_type=authorization_code) — verifica PKCE, emite tokens
 * </ol>
 *
 * <p>Login directo (API/CLI):
 * <ul>
 *   <li>POST /account/direct-login — email/password → tokens directamente
 * </ul>
 *
 * @author cmartinezs
 * @version 1.0
 */
@ResponseBody
@RequestMapping("/api/v1/platform")
@Tag(name = "Platform Auth", description = "Platform authentication endpoints (PKCE + direct login)")
public class PlatformAuthController {

  static final String SESSION_ATTR_AUTH_STATE = "platformAuthorizationState";
  static final String SESSION_ATTR_AUTH_CODE = "platformAuthCode";

  private final AuthenticatePlatformUserUseCase authenticateUseCase;
  private final IssuePlatformTokensUseCase issueTokensUseCase;
  private final RotatePlatformRefreshTokenUseCase rotateRefreshTokenUseCase;
  private final GetPlatformUserUseCase getPlatformUserUseCase;
  private final List<String> allowedRedirectUris;
  private final String applicationName;

  public PlatformAuthController(
      AuthenticatePlatformUserUseCase authenticateUseCase,
      IssuePlatformTokensUseCase issueTokensUseCase,
      RotatePlatformRefreshTokenUseCase rotateRefreshTokenUseCase,
      GetPlatformUserUseCase getPlatformUserUseCase,
      List<String> platformAllowedRedirectUris,
      String platformApplicationName) {
    this.authenticateUseCase = authenticateUseCase;
    this.issueTokensUseCase = issueTokensUseCase;
    this.rotateRefreshTokenUseCase = rotateRefreshTokenUseCase;
    this.getPlatformUserUseCase = getPlatformUserUseCase;
    this.allowedRedirectUris = platformAllowedRedirectUris;
    this.applicationName = platformApplicationName;
  }

  // ─── GET /oauth2/authorize ─────────────────────────────────────────────────

  @GetMapping("/oauth2/authorize")
  @Operation(
      summary = "Iniciar flujo de autorización PKCE",
      description = "Valida redirect_uri contra allowlist y code_challenge_method=S256. "
          + "Almacena estado PKCE en sesión HTTP.")
  public ResponseEntity<BaseResponse<PlatformAuthorizationData>> authorize(
      @RequestParam("redirect_uri") String redirectUri,
      @RequestParam(value = "scope", defaultValue = "openid profile platform") String scope,
      @RequestParam("code_challenge") String codeChallenge,
      @RequestParam(value = "code_challenge_method", defaultValue = "S256") String codeChallengeMethod,
      HttpServletRequest httpRequest) {

    // Validar redirect_uri contra allowlist
    if (!allowedRedirectUris.contains(redirectUri)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(BaseResponse.<PlatformAuthorizationData>builder()
              .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT,
                  "redirect_uri is not in the allowed list"))
              .build());
    }

    // Solo S256 soportado
    if (!"S256".equals(codeChallengeMethod)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(BaseResponse.<PlatformAuthorizationData>builder()
              .failure(ResponseHelper.message(ResponseCode.UNSUPPORTED_PKCE_METHOD,
                  "Only S256 code_challenge_method is supported"))
              .build());
    }

    // Validar que code_challenge no esté vacío
    if (codeChallenge == null || codeChallenge.isBlank()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(BaseResponse.<PlatformAuthorizationData>builder()
              .failure(ResponseHelper.message(ResponseCode.REQUIRED_FIELD_MISSING,
                  "code_challenge is required"))
              .build());
    }

    // Almacenar estado en sesión HTTP
    var sessionState = new PlatformAuthorizationSessionState(
        redirectUri, scope, codeChallenge, codeChallengeMethod);
    HttpSession session = httpRequest.getSession(true);
    session.setAttribute(SESSION_ATTR_AUTH_STATE, sessionState);

    var data = new PlatformAuthorizationData(applicationName, redirectUri);

    return ResponseEntity.ok(BaseResponse.<PlatformAuthorizationData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_AUTHORIZATION_INITIATED))
        .build());
  }

  // ─── POST /account/login ───────────────────────────────────────────────────

  @PostMapping("/account/login")
  @Operation(
      summary = "Login de plataforma (flujo PKCE)",
      description = "Autentica al usuario y genera un authorization code. "
          + "Requiere que se haya iniciado el flujo con GET /oauth2/authorize.")
  public ResponseEntity<BaseResponse<PlatformLoginData>> login(
      @RequestBody PlatformLoginRequest request,
      HttpServletRequest httpRequest) {

    // Recuperar estado de autorización de la sesión
    HttpSession session = httpRequest.getSession(false);
    if (session == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(BaseResponse.<PlatformLoginData>builder()
              .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT,
                  "No authorization session found. Start with GET /oauth2/authorize"))
              .build());
    }

    var authState = (PlatformAuthorizationSessionState) session.getAttribute(SESSION_ATTR_AUTH_STATE);
    if (authState == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(BaseResponse.<PlatformLoginData>builder()
              .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT,
                  "No authorization session found. Start with GET /oauth2/authorize"))
              .build());
    }

    // Autenticar usuario
    var command = new AuthenticatePlatformUserCommand(request.email(), request.password());
    PlatformUser platformUser = authenticateUseCase.execute(command);

    // Generar authorization code
    String code = UUID.randomUUID().toString();
    var authCodeState = new PlatformAuthCodeState(
        code, platformUser.getId().value(), Instant.now());
    session.setAttribute(SESSION_ATTR_AUTH_CODE, authCodeState);

    var data = new PlatformLoginData(
        "Authorization code issued", code, authState.redirectUri());

    return ResponseEntity.ok(BaseResponse.<PlatformLoginData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_LOGIN_SUCCESS))
        .build());
  }

  // ─── POST /oauth2/token ────────────────────────────────────────────────────

  @PostMapping("/oauth2/token")
  @Operation(
      summary = "Intercambio de tokens",
      description = "Soporta grant_type=authorization_code (PKCE) y grant_type=refresh_token")
  public ResponseEntity<BaseResponse<PlatformTokenData>> token(
      @RequestBody PlatformTokenRequest request,
      HttpServletRequest httpRequest) {

    return switch (request.grantType()) {
      case "authorization_code" -> handleAuthorizationCodeGrant(request, httpRequest);
      case "refresh_token" -> handleRefreshTokenGrant(request);
      default -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(BaseResponse.<PlatformTokenData>builder()
              .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT,
                  "Unsupported grant_type. Use 'authorization_code' or 'refresh_token'"))
              .build());
    };
  }

  // ─── POST /account/direct-login ────────────────────────────────────────────

  @PostMapping("/account/direct-login")
  @Operation(
      summary = "Login directo (API/CLI)",
      description = "Autenticación directa email/password → tokens (sin PKCE, sin sesión HTTP)")
  public ResponseEntity<BaseResponse<PlatformTokenData>> directLogin(
      @RequestBody PlatformLoginRequest request,
      HttpServletRequest httpRequest) {

    // Autenticar
    var command = new AuthenticatePlatformUserCommand(request.email(), request.password());
    PlatformUser platformUser = authenticateUseCase.execute(command);

    // Emitir tokens
    String issuer = buildIssuer(httpRequest);
    IssuePlatformTokensResult result = issueTokensUseCase.execute(
        platformUser, issuer,
        httpRequest.getHeader("User-Agent"),
        extractIpAddress(httpRequest));

    var data = new PlatformTokenData(
        result.accessToken(), result.refreshToken(),
        result.tokenType(), result.expiresIn());

    return ResponseEntity.ok(BaseResponse.<PlatformTokenData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_TOKEN_ISSUED))
        .build());
  }

  // ─── Grant handlers (privados) ─────────────────────────────────────────────

  private ResponseEntity<BaseResponse<PlatformTokenData>> handleAuthorizationCodeGrant(
      PlatformTokenRequest request, HttpServletRequest httpRequest) {

    HttpSession session = httpRequest.getSession(false);
    if (session == null) {
      return unauthorizedResponse("No session found for authorization code exchange");
    }

    var authState = (PlatformAuthorizationSessionState) session.getAttribute(SESSION_ATTR_AUTH_STATE);
    var authCodeState = (PlatformAuthCodeState) session.getAttribute(SESSION_ATTR_AUTH_CODE);

    if (authState == null || authCodeState == null) {
      return unauthorizedResponse("No authorization state found in session");
    }

    // Validar que el código coincida
    if (!authCodeState.code().equals(request.code())) {
      return unauthorizedResponse("Invalid authorization code");
    }

    // Validar redirect_uri coincide
    if (request.redirectUri() != null && !authState.redirectUri().equals(request.redirectUri())) {
      return unauthorizedResponse("redirect_uri does not match the original request");
    }

    // Verificar PKCE: SHA256(code_verifier) == code_challenge
    if (request.codeVerifier() == null || request.codeVerifier().isBlank()) {
      return unauthorizedResponse("code_verifier is required for PKCE");
    }

    String computedChallenge = computeS256Challenge(request.codeVerifier());
    if (!computedChallenge.equals(authState.codeChallenge())) {
      return unauthorizedResponse("PKCE verification failed: code_verifier does not match code_challenge");
    }

    // Invalidar authorization code (single-use)
    session.removeAttribute(SESSION_ATTR_AUTH_CODE);
    session.removeAttribute(SESSION_ATTR_AUTH_STATE);

    // Cargar usuario de plataforma y emitir tokens
    PlatformUser platformUser = getPlatformUserUseCase.execute(
        UserId.of(authCodeState.platformUserId()));

    String issuer = buildIssuer(httpRequest);
    IssuePlatformTokensResult result = issueTokensUseCase.execute(
        platformUser, issuer,
        httpRequest.getHeader("User-Agent"),
        extractIpAddress(httpRequest));

    var data = new PlatformTokenData(
        result.accessToken(), result.refreshToken(),
        result.tokenType(), result.expiresIn());

    return ResponseEntity.ok(BaseResponse.<PlatformTokenData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.PLATFORM_TOKEN_ISSUED))
        .build());
  }

  private ResponseEntity<BaseResponse<PlatformTokenData>> handleRefreshTokenGrant(
      PlatformTokenRequest request) {

    if (request.refreshToken() == null || request.refreshToken().isBlank()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(BaseResponse.<PlatformTokenData>builder()
              .failure(ResponseHelper.message(ResponseCode.REQUIRED_FIELD_MISSING,
                  "refresh_token is required"))
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

  // ─── Helpers ───────────────────────────────────────────────────────────────

  static String computeS256Challenge(String codeVerifier) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
          .digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private ResponseEntity<BaseResponse<PlatformTokenData>> unauthorizedResponse(String detail) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(BaseResponse.<PlatformTokenData>builder()
            .failure(ResponseHelper.message(ResponseCode.AUTHENTICATION_REQUIRED, detail))
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
