package io.cmartinezs.keygo.supabase.membership.repository;

import io.cmartinezs.keygo.supabase.membership.entity.MembershipEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for MembershipEntity.
 * <p>Repositorio Spring Data JPA para MembershipEntity.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface MembershipJpaRepository extends JpaRepository<MembershipEntity, UUID> {

  /**
   * Find a membership by user ID and client app ID.
   * <p>Encuentra una membresía por ID de usuario e ID de app de cliente.
   */
  Optional<MembershipEntity> findByUserIdAndClientAppId(UUID userId, UUID clientAppId);

  /**
   * List all memberships for a user.
   * <p>Lista todas las membresías de un usuario.
   */
  List<MembershipEntity> findByUserId(UUID userId);

  /**
   * List all memberships for a client app.
   * <p>Lista todas las membresías de una app de cliente.
   */
  List<MembershipEntity> findByClientAppId(UUID clientAppId);

  /**
   * Check if a membership exists.
   * <p>Verifica si una membresía existe.
   */
  boolean existsByUserIdAndClientAppId(UUID userId, UUID clientAppId);

  /**
   * Find role codes for the active membership of a given user and client app.
   * <p>Encuentra los códigos de rol de la membresía activa de un usuario en una app.
   */
  @Query(value =
      "SELECT ar.code FROM app_roles ar " +
      "JOIN membership_roles mr ON ar.id = mr.role_id " +
      "JOIN memberships m ON mr.membership_id = m.id " +
      "WHERE m.user_id = :userId AND m.client_app_id = :clientAppId AND m.status = 'ACTIVE'",
      nativeQuery = true)
  List<String> findRoleCodesByUserIdAndClientAppId(
      @Param("userId") UUID userId,
      @Param("clientAppId") UUID clientAppId);
}

