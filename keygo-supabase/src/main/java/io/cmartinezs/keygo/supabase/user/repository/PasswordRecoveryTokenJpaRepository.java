package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.supabase.user.entity.PasswordRecoveryTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for PasswordRecoveryTokenEntity.
 * <p>Repositorio JPA para PasswordRecoveryTokenEntity.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface PasswordRecoveryTokenJpaRepository extends JpaRepository<PasswordRecoveryTokenEntity, UUID> {

  /** Find a recovery token by its token string. */
  Optional<PasswordRecoveryTokenEntity> findByToken(String token);

  /** Find the token row by tenant user id (for upsert logic). */
  Optional<PasswordRecoveryTokenEntity> findByTenantUser_Id(UUID tenantUserId);

  /** Mark token as used. */
  @Modifying
  @Query("UPDATE PasswordRecoveryTokenEntity t SET t.usedAt = :usedAt WHERE t.token = :token")
  void markUsed(@Param("token") String token, @Param("usedAt") Instant usedAt);
}
