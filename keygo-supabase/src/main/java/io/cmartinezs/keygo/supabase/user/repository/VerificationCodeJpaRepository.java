package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.VerificationCodeEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para códigos basados en la tabla {@code email_verifications}.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface VerificationCodeJpaRepository extends JpaRepository<VerificationCodeEntity, UUID> {

  Optional<VerificationCodeEntity> findByPlatformUser_Id(UUID platformUserId);

  Optional<VerificationCodeEntity> findByCode(String code);

  /** Busca el más reciente para un platform_user con lock pesimista. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT v FROM VerificationCodeEntity v "
          + "WHERE v.platformUser = :user ORDER BY v.createdAt DESC LIMIT 1")
  Optional<VerificationCodeEntity> findLatestPlatformUserWithLock(
      @Param("user") PlatformUserEntity user);

  /** Marca el código como usado. */
  @Modifying
  @Query("UPDATE VerificationCodeEntity v SET v.usedAt = :usedAt WHERE v.id = :id")
  void markUsedById(@Param("id") UUID id, @Param("usedAt") Instant usedAt);

  /** Count pending verifications: not yet used AND not expired. */
  @Query("SELECT COUNT(v) FROM VerificationCodeEntity v WHERE v.usedAt IS NULL AND v.expiresAt > :now")
  long countPending(@Param("now") Instant now);

  default long countPendingByPurpose(String purpose, Instant now) {
    return countPending(now);
  }

  /** Count expired-but-unused verifications. */
  @Query("SELECT COUNT(v) FROM VerificationCodeEntity v WHERE v.usedAt IS NULL AND v.expiresAt <= :now")
  long countExpiredPending(@Param("now") Instant now);

  default long countExpiredPendingByPurpose(String purpose, Instant now) {
    return countExpiredPending(now);
  }

  /** Count verifications used after the given cutoff. */
  long countByUsedAtAfter(Instant cutoff);
}
