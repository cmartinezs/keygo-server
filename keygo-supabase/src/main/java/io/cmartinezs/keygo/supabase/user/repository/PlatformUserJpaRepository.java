package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link PlatformUserEntity}.
 *
 * <p>Provides CRUD operations and lookup methods for global platform users.
 */
@Repository
public interface PlatformUserJpaRepository extends JpaRepository<PlatformUserEntity, UUID> {

  Optional<PlatformUserEntity> findByEmail(String email);

  @Query(
      value =
          "SELECT * FROM platform_users pu "
              + "WHERE split_part(CAST(pu.email AS text), '@', 1) = :username "
              + "LIMIT 1",
      nativeQuery = true)
  Optional<PlatformUserEntity> findByUsername(@Param("username") String username);

  boolean existsByEmail(String email);

  @Query(
      value =
          "SELECT EXISTS ("
              + "SELECT 1 FROM platform_users pu "
              + "WHERE split_part(CAST(pu.email AS text), '@', 1) = :username"
              + ")",
      nativeQuery = true)
  boolean existsByUsername(@Param("username") String username);
}
