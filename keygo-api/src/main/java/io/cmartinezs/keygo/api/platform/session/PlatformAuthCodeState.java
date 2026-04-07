package io.cmartinezs.keygo.api.platform.session;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Estado del authorization code de plataforma almacenado en sesión HTTP.
 *
 * <p>Se genera en POST /platform/account/login tras autenticar al usuario
 * y se consume en POST /platform/oauth2/token (grant_type=authorization_code).
 *
 * @param code código de autorización único (UUID)
 * @param platformUserId ID del usuario de plataforma autenticado
 * @param createdAt instante de creación (para TTL)
 * @author cmartinezs
 * @version 1.0
 */
public record PlatformAuthCodeState(
    String code,
    UUID platformUserId,
    Instant createdAt)
    implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
}
