package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.command.ExchangeAuthorizationCodeCommand;
import io.cmartinezs.keygo.app.auth.port.AuthorizationCodeRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.result.ExchangeAuthorizationCodeResult;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.auth.exception.AuthorizationCodeExpiredException;
import io.cmartinezs.keygo.domain.auth.exception.InvalidAuthorizationCodeException;
import io.cmartinezs.keygo.domain.auth.exception.InvalidPkceVerificationException;
import io.cmartinezs.keygo.domain.auth.model.AuthorizationCode;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Caso de uso: Canjear un código de autorización por tokens.
 *
 * <p>Valida que:
 * <ul>
 *   <li>El código existe y está en estado PENDING
 *   <li>El código no ha expirado
 *   <li>El verifier PKCE es correcto (si el challenge es S256)
 *   <li>La app cliente coincide
 *   <li>La redirección coincide exactamente
 * </ul>
 *
 * <p>Resultado: confirmación de que el código fue canjeado. La emisión de tokens ocurre en Fase 6.
 */
public class ExchangeAuthorizationCodeUseCase {
  private final AuthorizationCodeRepositoryPort authorizationCodeRepository;
  private final ClientAppRepositoryPort clientAppRepository;
  private final TenantRepositoryPort tenantRepository;
  private final ClockPort clock;

  public ExchangeAuthorizationCodeUseCase(
      AuthorizationCodeRepositoryPort authorizationCodeRepository,
      ClientAppRepositoryPort clientAppRepository,
      TenantRepositoryPort tenantRepository,
      ClockPort clock) {
    this.authorizationCodeRepository = authorizationCodeRepository;
    this.clientAppRepository = clientAppRepository;
    this.tenantRepository = tenantRepository;
    this.clock = clock;
  }

  /**
   * Ejecuta el canje del código por token.
   *
   * @param command parámetros del comando
   * @return resultado con referencia al código canjeado
   * @throws InvalidAuthorizationCodeException si el código no existe
   * @throws AuthorizationCodeExpiredException si el código expiró
   * @throws InvalidPkceVerificationException si la verificación PKCE falla
   * @throws ClientAppNotFoundException si la app no existe
   * @throws IllegalStateException si el código ya fue usado o revocado
   */
  public ExchangeAuthorizationCodeResult execute(ExchangeAuthorizationCodeCommand command) {
    // Buscar el código
    AuthorizationCode authorizationCode =
        authorizationCodeRepository
            .findByCode(command.code())
            .orElseThrow(
                () ->
                    new InvalidAuthorizationCodeException(
                        "Authorization code not found or invalid"));

    // Validar que el código no ha expirado
    if (!authorizationCode.isNotExpired()) {
      throw new AuthorizationCodeExpiredException("Authorization code has expired");
    }

    // Validar que el código aún no fue usado
    if (authorizationCode.getStatus().equals(
        io.cmartinezs.keygo.domain.auth.model.AuthorizationCodeStatus.USED)) {
      throw new InvalidAuthorizationCodeException(
          "Authorization code has already been used");
    }

    // Validar tenant y app cliente
    TenantId tenantId =
        tenantRepository
            .findBySlug(new TenantSlug(command.tenantSlug()))
            .map(Tenant::getId)
            .orElseThrow(
                () ->
                    new InvalidAuthorizationCodeException(
                        "Tenant not found: " + command.tenantSlug()));

    if (!authorizationCode.getTenantId().equals(tenantId)) {
      throw new InvalidAuthorizationCodeException(
          "Authorization code does not belong to this tenant");
    }

    ClientApp clientApp =
        clientAppRepository
            .findByClientIdAndTenantId(new ClientId(command.clientId()), tenantId)
            .orElseThrow(
                () ->
                    new ClientAppNotFoundException(
                        "Client app not found: " + command.clientId()));

    if (!authorizationCode.getClientAppId().equals(clientApp.getId())) {
      throw new InvalidAuthorizationCodeException(
          "Authorization code was not issued to this client app");
    }

    // Validar redirect URI
    if (!authorizationCode.getRedirectUri().equals(command.redirectUri())) {
      throw new InvalidAuthorizationCodeException(
          "Redirect URI does not match the one in the authorization code");
    }

    // Validar PKCE
    validatePkce(authorizationCode, command.codeVerifier());

    // Marcar como usado
    authorizationCode.markAsUsed(clock.now());
    authorizationCodeRepository.update(authorizationCode);

    return new ExchangeAuthorizationCodeResult(
        authorizationCode.getId().id().toString(),
        authorizationCode.getUserId().value().toString(),
        clientApp.getClientId().value(),
        authorizationCode.getScopes().asString());
  }

  /** Valida el verifier PKCE contra el challenge. */
  private void validatePkce(AuthorizationCode authorizationCode, String codeVerifier) {
    String method = authorizationCode.getCodeChallenge().getMethod();
    String challenge = authorizationCode.getCodeChallenge().getChallenge();

    if ("plain".equals(method)) {
      // En plain, el verifier debe ser igual al challenge
      if (!codeVerifier.equals(challenge)) {
        throw new InvalidPkceVerificationException(
            "Code verifier does not match code challenge (plain)");
      }
    } else if ("S256".equals(method)) {
      // En S256, generar el hash del verifier y comparar
      String verifierHash = hashAndEncodeCodeVerifier(codeVerifier);
      if (!verifierHash.equals(challenge)) {
        throw new InvalidPkceVerificationException(
            "Code verifier does not match code challenge (S256)");
      }
    } else {
      throw new InvalidPkceVerificationException(
          "Unknown PKCE method: " + method);
    }
  }

  /** Realiza SHA256 del verifier y lo codifica en base64url. */
  private String hashAndEncodeCodeVerifier(String codeVerifier) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new InvalidPkceVerificationException("SHA-256 not available", e);
    }
  }
}


