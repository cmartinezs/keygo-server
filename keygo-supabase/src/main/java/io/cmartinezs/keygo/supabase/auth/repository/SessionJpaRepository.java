package io.cmartinezs.keygo.supabase.auth.repository;

import io.cmartinezs.keygo.supabase.auth.entity.SessionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionJpaRepository extends JpaRepository<SessionEntity, UUID> {

  long countByStatus(String status);

  @Query("SELECT s.status, COUNT(s) FROM SessionEntity s GROUP BY s.status")
  List<Object[]> countGroupByStatus();

  @Query(
      "SELECT s FROM SessionEntity s "
          + "WHERE s.platformUser.id = :platformUserId "
          + "ORDER BY s.lastAccessedAt DESC")
  List<SessionEntity> findAllByPlatformUserId(@Param("platformUserId") UUID platformUserId);

  @Query(
      "SELECT s FROM SessionEntity s "
          + "WHERE s.tenantUserId = :tenantUserId AND s.tenantId = :tenantId "
          + "ORDER BY s.lastAccessedAt DESC")
  List<SessionEntity> findAllByTenantUserIdAndTenantId(
      @Param("tenantUserId") UUID tenantUserId,
      @Param("tenantId") UUID tenantId);
}
