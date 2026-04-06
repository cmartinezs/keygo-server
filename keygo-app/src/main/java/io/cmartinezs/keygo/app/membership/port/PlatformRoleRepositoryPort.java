package io.cmartinezs.keygo.app.membership.port;

import io.cmartinezs.keygo.domain.membership.model.PlatformRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT: repository operations for platform-level roles.
 * <p>Puerto de salida: operaciones de repositorio para roles de plataforma.
 * @author cmartinezs
 * @version 1.0
 */
public interface PlatformRoleRepositoryPort {

  Optional<PlatformRole> findByCode(String code);

  List<PlatformRole> findAll();

  PlatformRole save(PlatformRole platformRole);

  void deleteById(UUID id);

  boolean existsByCode(String code);
}
