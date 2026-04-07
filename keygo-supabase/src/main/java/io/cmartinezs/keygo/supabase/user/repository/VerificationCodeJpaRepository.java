package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.VerificationCodeEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA unificado para {@link VerificationCodeEntity}.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface VerificationCodeJpaRepository extends JpaRepository<VerificationCodeEntity, UUID> {

  /** Busca por tenant_user_id y purpose. */
  Optional<VerificationCodeEntity> findByTenantUser_IdAndPurpose(UUID tenantUserId, String purpose);

  /** Busca por código y propósito. */
  Optional<VerificationCodeEntity> findByCodeAndPurpose(String code, String purpose);

  /** Busca el más reciente para un usuario + propósito, ordenado por created_at DESC. */
  Optional<VerificationCodeEntity> findTopByTenantUserAndPurposeOrderByCreatedAtDesc(
      TenantUserEntity tenantUser, String purpose);

  /** Igual al anterior pero con lock pesimista para upsertIfExpiredOrAbsent. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT v FROM VerificationCodeEntity v WHERE v.tenantUser = :user AND v.purpose = :purpose ORDER BY v.createdAt DESC LIMIT 1")
  Optional<VerificationCodeEntity> findLatestWithLock(
      @Param("user") TenantUserEntity user,
      @Param("purpose") String purpose);

  /** Marca el código como usado. */
  @Modifying
  @Query("UPDATE VerificationCodeEntity v SET v.usedAt = :usedAt WHERE v.id = :id")
  void markUsedById(@Param("id") UUID id, @Param("usedAt") Instant usedAt);

  /** Count pending verifications for a purpose: not yet used AND not expired. */
  @Query("SELECT COUNT(v) FROM VerificationCodeEntity v WHERE v.purpose = :purpose AND v.usedAt IS NULL AND v.expiresAt > :now")
  long countPendingByPurpose(@Param("purpose") String purpose, @Param("now") Instant now);

  /** Count expired-but-unused verifications for a purpose. */
  @Query("SELECT COUNT(v) FROM VerificationCodeEntity v WHERE v.purpose = :purpose AND v.usedAt IS NULL AND v.expiresAt <= :now")
  long countExpiredPendingByPurpose(@Param("purpose") String purpose, @Param("now") Instant now);

  /** Count verifications used after the given cutoff. */
  long countByUsedAtAfter(Instant cutoff);
}
