package io.cmartinezs.keygo.app.user.port;

import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;

import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT — contrato unificado de persistencia para códigos de verificación.
 *
 * <p>Consolida los anteriores {@code EmailVerificationRepositoryPort},
 * {@code PasswordResetCodeRepositoryPort} y {@code PasswordRecoveryTokenRepositoryPort}
 * en una sola interfaz discriminada por {@link VerificationPurpose}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public interface VerificationCodeRepositoryPort {

  /**
   * Persiste un nuevo código de verificación.
   * Si ya existe uno activo (no usado) para el mismo usuario + propósito,
   * la implementación debe actualizarlo (upsert).
   *
   * @param code el código a persistir
   * @return el código persistido
   */
  VerificationCode upsert(VerificationCode code);

  /**
   * Busca un código por su identificador UUID.
   *
   * @param id UUID de la fila
   * @return el código, o vacío si no existe
   */
  Optional<VerificationCode> findById(UUID id);

  /**
   * Busca el código activo (no usado) de un usuario para un propósito específico.
   *
   * @param userId  identificador del usuario
   * @param purpose propósito del código
   * @return el código, o vacío si no existe
   */
  Optional<VerificationCode> findByUserIdAndPurpose(UserId userId, VerificationPurpose purpose);

  /**
   * Busca un código por su valor de código/token y propósito.
   *
   * @param code    el valor del código o token
   * @param purpose propósito del código
   * @return el código, o vacío si no existe
   */
  Optional<VerificationCode> findByCodeAndPurpose(String code, VerificationPurpose purpose);

  /**
   * Marca el código como usado estableciendo {@code used_at = now()}.
   *
   * @param code el código a marcar como usado
   */
  void markUsed(VerificationCode code);

  /**
   * Atómicamente: si existe un código válido (no expirado, no usado) para el usuario + propósito,
   * lo retorna sin persistir nada. Si no, guarda {@code newCode} y lo retorna.
   *
   * <p>Las implementaciones deben ejecutar find+check+save dentro de una transacción
   * con lock pesimista para prevenir inserciones duplicadas concurrentes.
   *
   * @param userId  identificador del usuario
   * @param purpose propósito del código
   * @param newCode el código a persistir si no hay uno activo
   * @return el código activo — ya sea el preexistente o el recién guardado
   */
  VerificationCode upsertIfExpiredOrAbsent(UserId userId, VerificationPurpose purpose, VerificationCode newCode);
}
