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
import io.cmartinezs.keygo.app.auth.usecase.AuthenticateUserForAuthorizationUseCase;
import io.cmartinezs.keygo.app.auth.usecase.ExchangeAuthorizationCodeUseCase;
import io.cmartinezs.keygo.app.auth.usecase.InitiateAuthorizationUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueAuthorizationCodeUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueTokensUseCase;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller: OAuth 2.0 Authorization Flow endpoints.
 *
 * <p>Implementa los endpoints del flujo de autorización:
 * <ul>
 *   <li>GET /authorize — iniciar autorización
 *   <li>POST /account/login — enviar credenciales
 *   <li>POST /oauth2/token — canjear código por token
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}")
public class AuthorizationController {

  private final InitiateAuthorizationUseCase initiateAuthorizationUseCase;
  private final AuthenticateUserForAuthorizationUseCase authenticateUserForAuthorizationUseCase;
  private final IssueAuthorizationCodeUseCase issueAuthorizationCodeUseCase;
  private final ExchangeAuthorizationCodeUseCase exchangeAuthorizationCodeUseCase;
  private final IssueTokensUseCase issueTokensUseCase;
  private final String issuerBaseUrl;

  public AuthorizationController(
      InitiateAuthorizationUseCase initiateAuthorizationUseCase,
      AuthenticateUserForAuthorizationUseCase authenticateUserForAuthorizationUseCase,
      IssueAuthorizationCodeUseCase issueAuthorizationCodeUseCase,
      ExchangeAuthorizationCodeUseCase exchangeAuthorizationCodeUseCase,
      IssueTokensUseCase issueTokensUseCase,
      @Value("${keygo.info.issuer-base-url:http://localhost:8080/keygo-server}") String issuerBaseUrl) {
    this.initiateAuthorizationUseCase = initiateAuthorizationUseCase;
    this.authenticateUserForAuthorizationUseCase = authenticateUserForAuthorizationUseCase;
    this.issueAuthorizationCodeUseCase = issueAuthorizationCodeUseCase;
    this.exchangeAuthorizationCodeUseCase = exchangeAuthorizationCodeUseCase;
    this.issueTokensUseCase = issueTokensUseCase;
    this.issuerBaseUrl = issuerBaseUrl;
  }

  /**
   * GET /api/v1/tenants/{tenantSlug}/oauth2/authorize
   *
   * <p>Inicia el flujo de autorización OAuth 2.0. Valida que:
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
   * <p>Autentica el usuario y emite un authorization code temporal que el cliente puede canjear
   * por token.
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
    var authSessionState =
        (AuthorizationSessionState) session.getAttribute("authorizationState");
    if (authSessionState == null) {
      throw new IllegalArgumentException(
          "Authorization state not found in session. Call GET /authorize first.");
    }

    // Validar credenciales del usuario
    var command = new AuthenticateUserCommand(tenantSlug, request.emailOrUsername(), request.password());
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
   * <p>Canjea un código de autorización por token. En esta fase solo procesa authorization_code
   * grant y valida PKCE. Retorna el ID del código canjeado como confirmación de auditoría.
   *
   * <p>En Fase 6 se agregarán {@code access_token}, {@code token_type}, {@code expires_in} y
   * {@code refresh_token} al cuerpo de la respuesta.
   *
   * @param tenantSlug slug del tenant
   * @param request parámetros del canje
   * @return ID del código canjeado (tokens en Fase 6)
   */
  @PostMapping("/oauth2/token")
  public ResponseEntity<BaseResponse<TokenData>> exchangeAuthorizationCode(
      @PathVariable String tenantSlug, @Valid @RequestBody TokenRequest request) {

    // 1. Canjear el código — valida PKCE, tenant, client, redirect URI
    var exchangeResult = exchangeAuthorizationCodeUseCase.execute(
        new ExchangeAuthorizationCodeCommand(
            tenantSlug, request.clientId(), request.code(),
            request.redirectUri(), request.codeVerifier()));

    // 2. Emitir access_token + id_token firmados con RS256
    String issuer = issuerBaseUrl + "/api/v1/tenants/" + tenantSlug;
    var tokenResult = issueTokensUseCase.execute(
        issuer,
        exchangeResult.userId(),
        exchangeResult.clientId(),
        exchangeResult.scope(),
        null,   // nonce: no disponible en este flujo sin session; extensible en Fase 7
        null,   // email: extender cuando se agregue GetUserUseCase aquí
        null,
        exchangeResult.authorizationCodeId());

    var responseData = new TokenData(
        tokenResult.accessToken(),
        tokenResult.idToken(),
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
}
