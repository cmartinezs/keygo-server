package io.cmartinezs.keygo.supabase.auth.repository;

import io.cmartinezs.keygo.supabase.auth.entity.PlatformSessionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformSessionJpaRepository extends JpaRepository<PlatformSessionEntity, UUID> {}
