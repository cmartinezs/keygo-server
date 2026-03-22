package io.cmartinezs.keygo.app.auth.port;

import java.time.Instant;
import java.util.Map;

/**
 * Puerto OUT: construcción de claim maps estándar para access_token e id_token.
 */
public interface TokenClaimsFactoryPort {

  /**
   * Construye los claims del access_token (RFC 9068 / RFC 7519).
   *
   * @param issuer   URL del emisor (iss)
   * @param subject  identificador del usuario (sub)
   * @param audience identificador del cliente (aud)
   * @param scope    scopes otorgados, separados por espacio
   * @param jti      JWT ID único
   * @param issuedAt instante de emisión
   * @param expiresAt instante de expiración
   * @return mapa de claims listo para firmar
   */
  Map<String, Object> buildAccessTokenClaims(
      String issuer,
      String subject,
      String audience,
      String scope,
      String jti,
      Instant issuedAt,
      Instant expiresAt);

  /**
   * Construye los claims del id_token (OIDC Core 1.0).
   *
   * @param issuer      URL del emisor (iss)
   * @param subject     identificador del usuario (sub)
   * @param audience    identificador del cliente (aud)
   * @param jti         JWT ID único
   * @param issuedAt    instante de emisión
   * @param expiresAt   instante de expiración
   * @param nonce       nonce del flujo de autorización (puede ser null)
   * @param email       email del usuario (puede ser null)
   * @param name        nombre completo del usuario (puede ser null)
   * @param accessToken access_token ya firmado (para generar at_hash)
   * @return mapa de claims listo para firmar
   */
  Map<String, Object> buildIdTokenClaims(
      String issuer,
      String subject,
      String audience,
      String jti,
      Instant issuedAt,
      Instant expiresAt,
      String nonce,
      String email,
      String name,
      String accessToken);
}

