package io.cmartinezs.keygo.app.user.port;

import io.cmartinezs.keygo.domain.user.model.PasswordRecoveryToken;

import java.util.Optional;

/**
 * Port OUT — contract for password recovery token persistence.
 * <p>Puerto de salida — contrato para persistencia de tokens de recuperación de contraseña.
 * @author cmartinezs
 * @version 1.0
 */
public interface PasswordRecoveryTokenRepositoryPort {

  /**
   * Creates or updates the recovery token for the user.
   * Since only one active token per user is allowed ({@code UNIQUE(tenant_user_id)}),
   * this performs an upsert: updates if exists, inserts otherwise.
   *
   * @param token the recovery token to persist
   * @return the persisted token (with generated id and createdAt)
   */
  PasswordRecoveryToken upsert(PasswordRecoveryToken token);

  /**
   * Finds a recovery token by its token string.
   *
   * @param token the hex token string
   * @return the token, or empty if not found
   */
  Optional<PasswordRecoveryToken> findByToken(String token);

  /**
   * Marks the token as used by setting {@code used_at = now()}.
   *
   * @param token the token to mark as used
   */
  void markUsed(PasswordRecoveryToken token);
}
