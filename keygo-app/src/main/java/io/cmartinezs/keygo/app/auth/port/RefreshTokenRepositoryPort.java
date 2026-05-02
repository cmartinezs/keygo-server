package io.cmartinezs.keygo.app.auth.port;

import io.cmartinezs.keygo.domain.auth.model.RefreshToken;
import io.cmartinezs.keygo.domain.auth.model.RefreshTokenId;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import java.util.List;
import java.util.Optional;

/**
 * Puerto OUT: persistencia de refresh tokens.
 */
public interface RefreshTokenRepositoryPort {

  /**
   * Persiste un refresh token nuevo.
   *
   * @param refreshToken token a guardar
   * @return el token guardado
   */
  RefreshToken save(RefreshToken refreshToken);

  /**
   * Busca un refresh token por su hash BCrypt.
   *
   * @param tokenHash hash del token plano
   * @return el token si existe
   */
  Optional<RefreshToken> findByTokenHash(String tokenHash);

  /**
   * Busca un refresh token por su ID.
   *
   * @param id ID del token
   * @return el token si existe
   */
  Optional<RefreshToken> findById(RefreshTokenId id);

  /**
   * Actualiza un refresh token existente (por ejemplo para marcarlo como USED o REVOKED).
   *
   * @param refreshToken token con datos actualizados
   */
  void update(RefreshToken refreshToken);

  /**
   * Revoca todos los refresh tokens activos asociados a una sesión.
   *
   * @param sessionId ID de la sesión
   * @return lista de tokens revocados
   */
  List<RefreshToken> revokeAllForSession(SessionId sessionId);
}

