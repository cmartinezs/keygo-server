package io.cmartinezs.keygo.app.user.port;

import io.cmartinezs.keygo.domain.user.model.PasswordResetCode;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT — contrato de persistencia para códigos de verificación del flujo RESET_PASSWORD.
 *
 * <p>Un solo código activo por usuario ({@code UNIQUE tenant_user_id} en BD).
 * La operación de guardado es un upsert (invalida el código anterior si existe).
 *
 * @author cmartinezs
 * @version 1.0
 */
public interface PasswordResetCodeRepositoryPort {

  /**
   * Persiste o actualiza el código de reset para el usuario.
   * Si ya existe un código para este usuario, lo reemplaza (invalida el anterior).
   *
   * @param code el código a persistir
   * @return el código persistido
   */
  PasswordResetCode upsert(PasswordResetCode code);

  /**
   * Busca un código de reset por su identificador único (requestId).
   *
   * <p>Se usa en el endpoint público {@code POST /account/reset-password} para localizar
   * la solicitud a partir del ID devuelto en el 401 de login bloqueado.
   *
   * @param id UUID de la fila en {@code password_reset_codes}
   * @return el código, o vacío si no existe
   */
  Optional<PasswordResetCode> findById(UUID id);

  /**
   * Busca el código activo (no usado) del usuario.
   *
   * @param userId identificador del usuario
   * @return el código, o vacío si no existe
   */
  Optional<PasswordResetCode> findByUserId(UserId userId);

  /**
   * Marca el código como usado estableciendo {@code used_at = now()}.
   *
   * @param code el código a marcar como usado
   */
  void markUsed(PasswordResetCode code);
}

