package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.command.RevokeTokenCommand;
import io.cmartinezs.keygo.app.auth.exception.HashingUnavailableException;
import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenStatus;

/**
 * Caso de uso: revocar un token (RFC 7009).
 *
 * <p>Busca el refresh token por hash y, si existe y es ACTIVE, lo marca como REVOKED.
 * Si no existe o ya está revocado, retorna 200 igualmente (RFC 7009 §2.2: la revocación
 * debe ser idempotente y no revelar si un token existe).
 */
public class RevokeTokenUseCase {

  private final RefreshTokenRepositoryPort refreshTokenRepository;

  public RevokeTokenUseCase(RefreshTokenRepositoryPort refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  /**
   * Revoca el token indicado en el comando.
   *
   * @param command parámetros de la revocación
   */
  public void execute(RevokeTokenCommand command) {
    // Buscar todos los refresh tokens activos del sistema para el raw token recibido.
    // Como no podemos buscar por token plano directamente (no almacenamos el plano),
    // implementamos la búsqueda hashando el token plano con bcrypt y buscando por hash.
    // Sin embargo, BCrypt genera un salt diferente por cada hash, por lo que no podemos
    // comparar directamente en DB. La solución es almacenar el hash determinista (SHA-256)
    // del token plano como clave de búsqueda, y BCrypt solo para "segunda línea" de seguridad.
    //
    // En esta implementación se usa el hash SHA-256 del token como clave de búsqueda.
    // El campo `tokenHash` en la DB contiene el SHA-256 del token plano (hex).
    String tokenHash = hashToken(command.token());
    var optionalToken = refreshTokenRepository.findByTokenHash(tokenHash);

    if (optionalToken.isEmpty()) {
      // RFC 7009: si el token no existe, responder 200 (no revelar si existe)
      return;
    }

    var refreshToken = optionalToken.get();

    // Si ya está revocado o expirado, responder 200 igualmente (idempotente)
    if (refreshToken.getStatus() == RefreshTokenStatus.REVOKED
        || refreshToken.getStatus() == RefreshTokenStatus.EXPIRED
        || refreshToken.getStatus() == RefreshTokenStatus.USED) {
      return;
    }

    refreshToken.revoke();
    refreshTokenRepository.update(refreshToken);
  }

  /**
   * Genera un hash SHA-256 (hex) del token plano para búsqueda en DB.
   * Este hash es determinista a diferencia de BCrypt.
   */
  static String hashTokenStatic(String rawToken) {
    return hashToken(rawToken);
  }

  /**
   * Genera un hash SHA-256 (hex) del token plano para búsqueda en DB.
   * Este hash es determinista a diferencia de BCrypt.
   */
  private static String hashToken(String rawToken) {
    try {
      var digest = java.security.MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      var sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new HashingUnavailableException("SHA-256", e);
    }
  }
}


