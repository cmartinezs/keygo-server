package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.supabase.user.entity.PasswordResetTokenEntity;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
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

@Repository
public interface PasswordResetTokenJpaRepository
    extends JpaRepository<PasswordResetTokenEntity, UUID> {

  Optional<PasswordResetTokenEntity> findByPlatformUser_Id(UUID platformUserId);

  Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT t FROM PasswordResetTokenEntity t "
          + "WHERE t.platformUser = :user ORDER BY t.createdAt DESC LIMIT 1")
  Optional<PasswordResetTokenEntity> findLatestWithLock(@Param("user") PlatformUserEntity user);

  @Modifying
  @Query("UPDATE PasswordResetTokenEntity t SET t.usedAt = :usedAt WHERE t.id = :id")
  void markUsedById(@Param("id") UUID id, @Param("usedAt") Instant usedAt);
}
