package io.cmartinezs.keygo.supabase.auth.repository;

import io.cmartinezs.keygo.supabase.auth.entity.SessionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repositorio JPA para la tabla {@code sessions}.
 */
public interface SessionJpaRepository extends JpaRepository<SessionEntity, UUID> {

  /** Count sessions with the given status (ACTIVE, EXPIRED, TERMINATED). */
  long countByStatus(String status);

  /** Count sessions grouped by status (single GROUP BY query for dashboard). */
  @Query("SELECT s.status, COUNT(s) FROM SessionEntity s GROUP BY s.status")
  List<Object[]> countGroupByStatus();
}

