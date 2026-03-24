package io.cmartinezs.keygo.api.auth.controller;

import io.cmartinezs.keygo.api.auth.request.AuthorizationRequest;
import io.cmartinezs.keygo.api.auth.request.LoginRequest;
import io.cmartinezs.keygo.api.auth.request.TokenRequest;
import io.cmartinezs.keygo.api.auth.response.AuthorizationInitiatedData;
import io.cmartinezs.keygo.api.auth.response.LoginData;
import io.cmartinezs.keygo.api.auth.response.TokenData;
import io.cmartinezs.keygo.api.auth.session.AuthorizationSessionState;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.auth.command.AuthenticateUserCommand;
import io.cmartinezs.keygo.app.auth.command.ExchangeAuthorizationCodeCommand;
import io.cmartinezs.keygo.app.auth.command.InitiateAuthorizationCommand;
import io.cmartinezs.keygo.app.auth.command.IssueAuthorizationCodeCommand;
import io.cmartinezs.keygo.app.auth.command.IssueClientCredentialsTokenCommand;
import io.cmartinezs.keygo.app.auth.command.OpenSessionCommand;
import io.cmartinezs.keygo.app.auth.command.RotateRefreshTokenCommand;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.app.auth.result.IssueTokensResult;
import io.cmartinezs.keygo.app.auth.usecase.AuthenticateUserForAuthorizationUseCase;
import io.cmartinezs.keygo.app.auth.usecase.ExchangeAuthorizationCodeUseCase;
import io.cmartinezs.keygo.app.auth.usecase.InitiateAuthorizationUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueAuthorizationCodeUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueClientCredentialsTokenUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueTokensUseCase;
import io.cmartinezs.keygo.app.auth.usecase.OpenSessionUseCase;
import io.cmartinezs.keygo.app.auth.usecase.RotateRefreshTokenUseCase;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.RefreshToken;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.model.UserId;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller: OAuth 2.0 Authorization Flow endpoints.
 *
 * <p>Implementa los endpoints del flujo de autorización:
 *
 * <ul>
 *   <li>GET /authorize — iniciar autorización
 *   <li>POST /account/login — enviar credenciales
 *   <li>POST /oauth2/token — canjear código por token
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}")
public class AuthorizationController {

  private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

  private final InitiateAuthorizationUseCase initiateAuthorizationUseCase;
  private final AuthenticateUserForAuthorizationUseCase authenticateUserForAuthorizationUseCase;
  private final IssueAuthorizationCodeUseCase issueAuthorizationCodeUseCase;
  private final ExchangeAuthorizationCodeUseCase exchangeAuthorizationCodeUseCase;
  private final IssueTokensUseCase issueTokensUseCase;
  private final IssueClientCredentialsTokenUseCase issueClientCredentialsTokenUseCase;
  private final OpenSessionUseCase openSessionUseCase;
  private final RotateRefreshTokenUseCase rotateRefreshTokenUseCase;
  private final RefreshTokenRepositoryPort refreshTokenRepository;
  private final UserRepositoryPort userRepository;
  private final TenantRepositoryPort tenantRepository;
  private final ClientAppRepositoryPort clientAppRepository;
  private final MembershipRepositoryPort membershipRepository;
  private final ClockPort clock;
  private final String issuerBaseUrl;

  private final SecureRandom random = new SecureRandom();

  public AuthorizationController(
      InitiateAuthorizationUseCase initiateAuthorizationUseCase,
      AuthenticateUserForAuthorizationUseCase authenticateUserForAuthorizationUseCase,
      IssueAuthorizationCodeUseCase issueAuthorizationCodeUseCase,
      ExchangeAuthorizationCodeUseCase exchangeAuthorizationCodeUseCase,
      IssueTokensUseCase issueTokensUseCase,
      IssueClientCredentialsTokenUseCase issueClientCredentialsTokenUseCase,
      OpenSessionUseCase openSessionUseCase,
      RotateRefreshTokenUseCase rotateRefreshTokenUseCase,
      RefreshTokenRepositoryPort refreshTokenRepository,
      UserRepositoryPort userRepository,
      TenantRepositoryPort tenantRepository,
      ClientAppRepositoryPort clientAppRepository,
      MembershipRepositoryPort membershipRepository,
      ClockPort clock,
      @Value("${keygo.info.issuer-base-url:http://localhost:8080/keygo-server}")
          String issuerBaseUrl) {
    this.initiateAuthorizationUseCase = initiateAuthorizationUseCase;
    this.authenticateUserForAuthorizationUseCase = authenticateUserForAuthorizationUseCase;
    this.issueAuthorizationCodeUseCase = issueAuthorizationCodeUseCase;
    this.exchangeAuthorizationCodeUseCase = exchangeAuthorizationCodeUseCase;
    this.issueTokensUseCase = issueTokensUseCase;
    this.issueClientCredentialsTokenUseCase = issueClientCredentialsTokenUseCase;
    this.openSessionUseCase = openSessionUseCase;
    this.rotateRefreshTokenUseCase = rotateRefreshTokenUseCase;
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
    this.tenantRepository = tenantRepository;
    this.clientAppRepository = clientAppRepository;
    this.membershipRepository = membershipRepository;
    this.clock = clock;
    this.issuerBaseUrl = issuerBaseUrl;
  }

  /**
   * GET /api/v1/tenants/{tenantSlug}/oauth2/authorize
   *
   * <p>Inicia el flujo de autorización OAuth 2.0. Valida que:
   *
   * <ul>
   *   <li>El tenant existe y está activo
   *   <li>La app cliente existe y pertenece al tenant
   *   <li>La redirect URI está registrada
   * </ul>
   *
   * <p>Guarda el estado de autorización en sesión para que POST /account/login pueda recuperarlo.
   *
   * @param tenantSlug slug del tenant
   * @param request parámetros de autorización
   * @param session sesión HTTP
   * @return respuesta con datos de la app cliente
   */
  @GetMapping("/oauth2/authorize")
  public ResponseEntity<BaseResponse<AuthorizationInitiatedData>> authorize(
      @PathVariable String tenantSlug,
      @Valid @ModelAttribute AuthorizationRequest request,
      HttpSession session) {

    var command =
        new InitiateAuthorizationCommand(
            tenantSlug,
            request.clientId(),
            request.redirectUri(),
            request.scope(),
            request.state(),
            request.codeChallenge(),
            request.codeChallengeMethod());

    var result = initiateAuthorizationUseCase.execute(command);

    // Guardar el estado en sesión para recuperarlo en POST /account/login
    var authSessionState =
        new AuthorizationSessionState(
            tenantSlug,
            request.clientId(),
            request.redirectUri(),
            request.scope(),
            request.codeChallenge(),
            request.codeChallengeMethod());
    session.setAttribute("authorizationState", authSessionState);

    var responseData =
        new AuthorizationInitiatedData(
            result.clientId(), result.clientName(), result.redirectUri());

    BaseResponse<AuthorizationInitiatedData> response =
        BaseResponse.<AuthorizationInitiatedData>builder()
            .data(responseData)
            .success(ResponseHelper.message(ResponseCode.AUTHORIZATION_INITIATED))
            .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * POST /api/v1/tenants/{tenantSlug}/account/login
   *
   * <p>Envía credenciales para autenticarse. Debe venir después de GET /authorize (que guarda el
   * estado en sesión).
   *
   * <p>Autentica el usuario y emite un authorization code temporal que el cliente puede canjear por
   * token.
   *
   * @param tenantSlug slug del tenant
   * @param request credenciales
   * @param session sesión HTTP (contiene estado de autorización)
   * @return authorization code emitido
   */
  @PostMapping("/account/login")
  public ResponseEntity<BaseResponse<LoginData>> login(
      @PathVariable String tenantSlug,
      @Valid @RequestBody LoginRequest request,
      HttpSession session) {

    // Recuperar el estado de autorización guardado en GET /authorize
    var authSessionState = (AuthorizationSessionState) session.getAttribute("authorizationState");
    if (authSessionState == null) {
      throw new IllegalArgumentException(
          "Authorization state not found in session. Call GET /authorize first.");
    }

    // Validar credenciales del usuario
    var command =
        new AuthenticateUserCommand(tenantSlug, request.emailOrUsername(), request.password());
    var user = authenticateUserForAuthorizationUseCase.execute(tenantSlug, command);

    // Emitir el authorization code usando el estado recuperado
    var issueCodeCommand =
        new IssueAuthorizationCodeCommand(
            authSessionState.tenantSlug(),
            authSessionState.clientId(),
            user.getId().value().toString(),
            authSessionState.redirectUri(),
            authSessionState.scope(),
            authSessionState.codeChallenge(),
            authSessionState.codeChallengeMethod());

    var authCodeResult = issueAuthorizationCodeUseCase.execute(issueCodeCommand);

    var responseData =
        new LoginData("Login successful", authCodeResult.code(), authCodeResult.redirectUri());

    BaseResponse<LoginData> response =
        BaseResponse.<LoginData>builder()
            .data(responseData)
            .success(ResponseHelper.message(ResponseCode.LOGIN_SUCCESSFUL))
            .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  /**
   * POST /api/v1/tenants/{tenantSlug}/oauth2/token
   *
   * <p>Soporta tres grant types:
   *
   * <ul>
   *   <li>{@code authorization_code} — canjea el código por tokens y emite refresh token
   *   <li>{@code refresh_token} — rota el refresh token y emite nuevos tokens
   *   <li>{@code client_credentials} — emite access_token para apps M2M (sin usuario final)
   * </ul>
   */
  @PostMapping("/oauth2/token")
  public ResponseEntity<BaseResponse<TokenData>> token(
      @PathVariable String tenantSlug, @Valid @RequestBody TokenRequest request) {

    String grantType = request.resolvedGrantType();

    if ("refresh_token".equals(grantType)) {
      return handleRefreshTokenGrant(tenantSlug, request);
    }

    if ("client_credentials".equals(grantType)) {
      return handleClientCredentialsGrant(tenantSlug, request);
    }

    // Default: authorization_code
    return handleAuthorizationCodeGrant(tenantSlug, request);
  }

  // ─── Grant: client_credentials ───────────────────────────────────────────

  private ResponseEntity<BaseResponse<TokenData>> handleClientCredentialsGrant(
      String tenantSlug, TokenRequest request) {

    if (request.clientSecret() == null || request.clientSecret().isBlank()) {
      throw new IllegalArgumentException("client_secret is required for client_credentials grant");
    }

    var command = new IssueClientCredentialsTokenCommand(
        tenantSlug,
        request.clientId(),
        request.clientSecret(),
        request.scope());

    var result = issueClientCredentialsTokenUseCase.execute(command);

    // Sin id_token, sin refresh_token, sin authorization_code_id — excluidos por NON_NULL Jackson
    var responseData = new TokenData(
        result.accessToken(),
        null,
        null,
        result.tokenType(),
        result.expiresIn(),
        result.scope(),
        null);

    BaseResponse<TokenData> response = BaseResponse.<TokenData>builder()
        .data(responseData)
        .success(ResponseHelper.message(ResponseCode.CLIENT_CREDENTIALS_TOKEN_ISSUED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // ─── Grant: authorization_code ────────────────────────────────────────────

  private ResponseEntity<BaseResponse<TokenData>> handleAuthorizationCodeGrant(
      String tenantSlug, TokenRequest request) {

    if (request.code() == null || request.code().isBlank()) {
      throw new IllegalArgumentException("code is required for authorization_code grant");
    }
    if (request.redirectUri() == null || request.redirectUri().isBlank()) {
      throw new IllegalArgumentException("redirect_uri is required for authorization_code grant");
    }

    // 1. Canjear el código
    var exchangeResult =
        exchangeAuthorizationCodeUseCase.execute(
            new ExchangeAuthorizationCodeCommand(
                tenantSlug,
                request.clientId(),
                request.code(),
                request.redirectUri(),
                request.codeVerifier()));

    // 2. Obtener datos del usuario y roles para los claims
    Instant now = clock.now();
    String email = null;
    String name = null;
    List<String> roles = List.of();
    var tenantOpt = tenantRepository.findBySlug(new TenantSlug(tenantSlug));
    if (tenantOpt.isPresent()) {
      TenantId tenantId = tenantOpt.get().getId();
      UUID userUUID = UUID.fromString(exchangeResult.userId());
      var userOpt = userRepository.findByIdAndTenantId(new UserId(userUUID), tenantId);
      if (userOpt.isPresent()) {
        var user = userOpt.get();
        email = user.getEmail() != null ? user.getEmail().value() : null;
        name = buildName(user.getFirstName(), user.getLastName());
      }
      // Resolver clientApp UUID para obtener roles de la membresía
      var clientAppOpt = clientAppRepository.findByClientIdAndTenantId(
          new ClientId(exchangeResult.clientId()), tenantId);
      if (clientAppOpt.isPresent()) {
        UUID clientAppUUID = clientAppOpt.get().getId().value();
        roles = membershipRepository.findRoleCodesByUserAndClientApp(userUUID, clientAppUUID);
      }
    }

    // 3. Emitir access_token + id_token (incluye claim roles)
    String issuer = issuerBaseUrl + "/api/v1/tenants/" + tenantSlug;
    IssueTokensResult tokenResult =
        issueTokensUseCase.execute(
            issuer,
            exchangeResult.userId(),
            exchangeResult.clientId(),
            exchangeResult.scope(),
            null,
            email,
            name,
            exchangeResult.authorizationCodeId(),
            roles);

    // 4. Abrir sesión
    Instant sessionExpiresAt = now.plus(REFRESH_TOKEN_TTL);
    String tenantIdStr = resolveTenantId(tenantSlug);
    String clientAppIdStr = resolveClientAppId(tenantIdStr, request.clientId());
    var sessionResult =
        openSessionUseCase.execute(
            new OpenSessionCommand(
                tenantIdStr,
                clientAppIdStr,
                exchangeResult.userId(),
                sessionExpiresAt,
                now,
                null,
                null));

    // 5. Generar refresh token
    String rawRefreshToken = generateSecureToken();
    String tokenHash = sha256Hex(rawRefreshToken);
    var refreshToken =
        RefreshToken.issue(
            tokenHash,
            new TenantId(UUID.fromString(tenantIdStr)),
            new ClientAppId(UUID.fromString(clientAppIdStr)),
            new UserId(UUID.fromString(exchangeResult.userId())),
            SessionId.from(UUID.fromString(sessionResult.sessionId())),
            exchangeResult.scope(),
            sessionExpiresAt,
            now);
    refreshTokenRepository.save(refreshToken);

    var responseData =
        new TokenData(
            tokenResult.accessToken(),
            tokenResult.idToken(),
            rawRefreshToken,
            tokenResult.tokenType(),
            tokenResult.expiresIn(),
            tokenResult.scope(),
            tokenResult.authorizationCodeId());

    BaseResponse<TokenData> response =
        BaseResponse.<TokenData>builder()
            .data(responseData)
            .success(ResponseHelper.message(ResponseCode.TOKEN_ISSUED))
            .build();
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // ─── Grant: refresh_token ─────────────────────────────────────────────────

  private ResponseEntity<BaseResponse<TokenData>> handleRefreshTokenGrant(
      String tenantSlug, TokenRequest request) {

    if (request.refreshToken() == null || request.refreshToken().isBlank()) {
      throw new IllegalArgumentException("refresh_token is required for refresh_token grant");
    }

    var rotateResult =
        rotateRefreshTokenUseCase.execute(
            new RotateRefreshTokenCommand(
                tenantSlug, request.clientId(), request.refreshToken(), request.scope()));

    var responseData =
        new TokenData(
            rotateResult.accessToken(),
            rotateResult.idToken(),
            rotateResult.rawRefreshToken(),
            rotateResult.tokenType(),
            rotateResult.expiresIn(),
            rotateResult.scope(),
            null);

    BaseResponse<TokenData> response =
        BaseResponse.<TokenData>builder()
            .data(responseData)
            .success(ResponseHelper.message(ResponseCode.REFRESH_TOKEN_ROTATED))
            .build();
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  private String resolveTenantId(String tenantSlug) {
    return tenantRepository
        .findBySlug(new TenantSlug(tenantSlug))
        .map(t -> t.getId().value().toString())
        .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantSlug));
  }

  private String resolveClientAppId(String tenantIdStr, String clientId) {
    TenantId tenantId = new TenantId(UUID.fromString(tenantIdStr));
    return clientAppRepository
        .findByClientIdAndTenantId(new ClientId(clientId), tenantId)
        .map(app -> app.getId().value().toString())
        .orElseThrow(() -> new IllegalArgumentException("ClientApp not found: " + clientId));
  }

  private String buildName(String firstName, String lastName) {
    if (firstName == null && lastName == null) return null;
    if (firstName == null) return lastName;
    if (lastName == null) return firstName;
    return firstName + " " + lastName;
  }

  private String generateSecureToken() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  static String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      var sb = new StringBuilder(hash.length * 2);
      for (byte b : hash) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
