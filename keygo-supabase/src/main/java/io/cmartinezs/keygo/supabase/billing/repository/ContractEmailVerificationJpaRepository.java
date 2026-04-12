package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.ContractEmailVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ContractEmailVerificationJpaRepository
    extends JpaRepository<ContractEmailVerificationEntity, UUID> {

  Optional<ContractEmailVerificationEntity> findByContract_Id(UUID contractId);

  @Modifying
  @Query("update ContractEmailVerificationEntity v set v.usedAt = :usedAt where v.id = :id")
  void markUsedById(@Param("id") UUID id, @Param("usedAt") OffsetDateTime usedAt);
}
