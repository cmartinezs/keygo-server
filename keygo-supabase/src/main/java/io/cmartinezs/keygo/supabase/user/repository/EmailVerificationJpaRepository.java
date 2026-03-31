package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.supabase.user.entity.EmailVerificationEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for EmailVerificationEntity.
 * <p>Repositorio JPA para EmailVerificationEntity.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerificationEntity, UUID> {

  /**
   * Find the most recent verification for a given tenant user, ordered by creation date descending.
   */
  Optional<EmailVerificationEntity> findTopByTenantUserOrderByCreatedAtDesc(TenantUserEntity tenantUser);

  /**
   * Same as {@link #findTopByTenantUserOrderByCreatedAtDesc} but acquires a pessimistic write lock.
   * Must be called within an active transaction.
   * <p>Igual que el método anterior pero adquiere un lock pesimista de escritura.
   * Debe llamarse dentro de una transacción activa.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT e FROM EmailVerificationEntity e WHERE e.tenantUser = :user ORDER BY e.createdAt DESC LIMIT 1")
  Optional<EmailVerificationEntity> findTopByTenantUserOrderByCreatedAtDescWithLock(
      @Param("user") TenantUserEntity user);

  /** Count pending verifications: not yet used AND not expired. */
  @Query("SELECT COUNT(e) FROM EmailVerificationEntity e WHERE e.usedAt IS NULL AND e.expiresAt > :now")
  long countPendingVerifications(@Param("now") Instant now);

  /** Count expired-but-unused verifications: not used AND already expired. */
  @Query("SELECT COUNT(e) FROM EmailVerificationEntity e WHERE e.usedAt IS NULL AND e.expiresAt <= :now")
  long countExpiredPendingVerifications(@Param("now") Instant now);

  /** Count verifications that were used after the given cutoff. */
  long countByUsedAtAfter(Instant cutoff);
}

