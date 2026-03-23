package io.cmartinezs.keygo.supabase.auth.repository;

import io.cmartinezs.keygo.supabase.auth.entity.SessionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la tabla {@code sessions}.
 */
public interface SessionJpaRepository extends JpaRepository<SessionEntity, UUID> {}

