package io.cmartinezs.keygo.supabase.auth.repository;

import io.cmartinezs.keygo.supabase.auth.entity.AuthorizationCodeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizationCodeJpaRepository extends JpaRepository<AuthorizationCodeEntity, UUID> {

  Optional<AuthorizationCodeEntity> findByCodeHash(String codeHash);

  long countByStatus(String status);

  @Query("SELECT ac.status, COUNT(ac) FROM AuthorizationCodeEntity ac GROUP BY ac.status")
  List<Object[]> countGroupByStatus();
}
