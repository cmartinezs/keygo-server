package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link PlatformUserEntity}.
 *
 * <p>Provides CRUD operations and lookup methods for global platform users.
 */
@Repository
public interface PlatformUserJpaRepository extends JpaRepository<PlatformUserEntity, UUID> {

  Optional<PlatformUserEntity> findByEmail(String email);

  Optional<PlatformUserEntity> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
