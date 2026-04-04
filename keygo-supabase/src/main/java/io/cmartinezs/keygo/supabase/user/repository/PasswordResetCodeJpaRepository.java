package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.supabase.user.entity.PasswordResetCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para {@link PasswordResetCodeEntity}.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface PasswordResetCodeJpaRepository extends JpaRepository<PasswordResetCodeEntity, UUID> {

  /** Busca el registro por tenant_user_id. */
  Optional<PasswordResetCodeEntity> findByTenantUser_Id(UUID tenantUserId);

  /** Marca el código como usado. */
  @Modifying
  @Query("UPDATE PasswordResetCodeEntity c SET c.usedAt = :usedAt WHERE c.id = :id")
  void markUsedById(@Param("id") UUID id, @Param("usedAt") Instant usedAt);
}

