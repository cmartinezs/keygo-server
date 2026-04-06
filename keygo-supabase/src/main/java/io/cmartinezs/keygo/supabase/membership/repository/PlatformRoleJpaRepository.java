package io.cmartinezs.keygo.supabase.membership.repository;

import io.cmartinezs.keygo.supabase.membership.entity.PlatformRoleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for PlatformRoleEntity.
 * <p>Repositorio Spring Data JPA para PlatformRoleEntity.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface PlatformRoleJpaRepository extends JpaRepository<PlatformRoleEntity, UUID> {

  Optional<PlatformRoleEntity> findByCode(String code);

  boolean existsByCode(String code);
}
