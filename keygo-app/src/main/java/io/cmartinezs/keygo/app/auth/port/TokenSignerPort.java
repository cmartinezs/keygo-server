package io.cmartinezs.keygo.app.auth.port;

import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import java.util.Map;

/**
 * Puerto OUT: firma de tokens JWT con clave RSA.
 */
public interface TokenSignerPort {

  /**
   * Firma un JWT y retorna el token en formato compacto (header.payload.signature).
   *
   * @param claims  claims del payload (iss, sub, aud, exp, iat, jti, etc.)
   * @param signingKey clave activa (contiene kid y material privado PEM)
   * @return JWT compacto firmado
   */
  String signJwt(Map<String, Object> claims, SigningKey signingKey);
}

