package io.cmartinezs.keygo.app.auth.result;

/**
 * Resultado del discovery document OIDC (openid-configuration).
 *
 * <p>Los campos siguen la especificación OpenID Connect Discovery 1.0.
 *
 * @param issuer                    URL del emisor
 * @param authorizationEndpoint     endpoint de autorización
 * @param tokenEndpoint             endpoint de tokens
 * @param jwksUri                   URL del JWKS endpoint
 * @param userinfoEndpoint          endpoint de userinfo
 * @param responseTypesSupported    tipos de respuesta soportados
 * @param subjectTypesSupported     tipos de subject soportados
 * @param idTokenSigningAlgSupported algoritmos de firma soportados para id_token
 * @param scopesSupported           scopes soportados
 * @param tokenEndpointAuthMethodsSupported métodos de autenticación en token endpoint
 * @param grantTypesSupported       grant types soportados
 * @param claimsSupported           claims soportados
 */
public record OidcConfigurationResult(
    String issuer,
    String authorizationEndpoint,
    String tokenEndpoint,
    String jwksUri,
    String userinfoEndpoint,
    java.util.List<String> responseTypesSupported,
    java.util.List<String> subjectTypesSupported,
    java.util.List<String> idTokenSigningAlgSupported,
    java.util.List<String> scopesSupported,
    java.util.List<String> tokenEndpointAuthMethodsSupported,
    java.util.List<String> grantTypesSupported,
    java.util.List<String> claimsSupported) {}

