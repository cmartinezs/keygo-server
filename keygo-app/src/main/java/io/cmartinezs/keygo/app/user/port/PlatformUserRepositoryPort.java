package io.cmartinezs.keygo.app.user.port;

import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.Optional;

/**
 * Port OUT: repository operations for global platform users.
 * <p>Puerto de salida: operaciones de repositorio para usuarios globales de la plataforma.
 * Email and username are globally unique (not scoped to any tenant).
 *
 * @author cmartinezs
 * @version 1.0
 */
public interface PlatformUserRepositoryPort {

  PlatformUser save(PlatformUser user);

  Optional<PlatformUser> findByEmail(EmailAddress email);

  Optional<PlatformUser> findByUsername(Username username);

  Optional<PlatformUser> findById(UserId userId);

  boolean existsByEmail(EmailAddress email);

  boolean existsByUsername(Username username);
}
