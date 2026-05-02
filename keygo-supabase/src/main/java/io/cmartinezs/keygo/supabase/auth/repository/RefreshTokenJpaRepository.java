package io.cmartinezs.keygo.supabase.auth.repository;

import io.cmartinezs.keygo.supabase.auth.entity.RefreshTokenEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio JPA para la tabla {@code refresh_tokens}.
 */
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

  Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

  List<RefreshTokenEntity> findBySession_Id(UUID sessionId);

  @Modifying
  @Query("UPDATE RefreshTokenEntity rt SET rt.status = 'REVOKED' WHERE rt.session.id = :sessionId AND rt.status = 'ACTIVE'")
  int revokeAllActiveBySessionId(@Param("sessionId") UUID sessionId);

  /** Count refresh tokens with the given status (ACTIVE, USED, EXPIRED, REVOKED). */
  long countByStatus(String status);

  /** Count refresh tokens grouped by status (single GROUP BY query for dashboard). */
  @Query("SELECT rt.status, COUNT(rt) FROM RefreshTokenEntity rt GROUP BY rt.status")
  List<Object[]> countGroupByStatus();
}

