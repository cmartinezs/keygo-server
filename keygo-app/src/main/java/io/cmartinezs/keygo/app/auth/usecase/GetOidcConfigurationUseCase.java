package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.result.OidcConfigurationResult;
import java.util.List;

/**
 * Caso de uso: construir el OIDC Discovery Document para un tenant.
 *
 * <p>El documento cumple OpenID Connect Discovery 1.0 y es consumido por librerías OAuth2
 * de terceros para auto-configurarse.
 */
public class GetOidcConfigurationUseCase {

  private final String issuerBaseUrl;

  public GetOidcConfigurationUseCase(String issuerBaseUrl) {
    this.issuerBaseUrl = issuerBaseUrl;
  }

  /**
   * Genera el discovery document para el tenant indicado.
   *
   * @param tenantSlug slug del tenant
   * @return resultado con todos los campos OIDC obligatorios
   */
  public OidcConfigurationResult execute(String tenantSlug) {
    String tenantBase = issuerBaseUrl + "/api/v1/tenants/" + tenantSlug;
    String issuer = tenantBase;

    return new OidcConfigurationResult(
        issuer,
        tenantBase + "/oauth2/authorize",
        tenantBase + "/oauth2/token",
        tenantBase + "/.well-known/jwks.json",
        tenantBase + "/userinfo",
        List.of("code"),
        List.of("public"),
        List.of("RS256"),
        List.of("openid", "profile", "email"),
        List.of("none", "client_secret_basic", "client_secret_post"),
        List.of("authorization_code"),
        List.of("sub", "iss", "aud", "exp", "iat", "jti", "email", "name", "scope"));
  }
}

