package io.cmartinezs.keygo.app.membership.port;

import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT for membership persistence operations.
 * <p>Puerto OUT para operaciones de persistencia de membresías.
 * Implementations are responsible for retrieving and storing Membership aggregates.
 * <p>Las implementaciones son responsables de recuperar y guardar agregados de Membership.
 * @author cmartinezs
 * @version 1.0
 */
public interface MembershipRepositoryPort {

  /**
   * Find a membership by ID.
   * <p>Encuentra una membresía por ID.
   * @param membershipId the membership ID
   * @return the membership, or empty if not found
   */
  Optional<Membership> findById(MembershipId membershipId);

  /**
   * Find a membership by user and client app IDs.
   * <p>Encuentra una membresía por IDs de usuario y app de cliente.
   * @param userId the user ID as UUID
   * @param clientAppId the client app ID as UUID
   * @return the membership, or empty if not found
   */
  Optional<Membership> findByUserAndClientApp(UUID userId, UUID clientAppId);

  /**
   * List all memberships for a given user.
   * <p>Lista todas las membresías de un usuario.
   * @param userId the user ID
   * @return list of memberships
   */
  List<Membership> findByUserId(UUID userId);

  /**
   * List all memberships for a given client app.
   * <p>Lista todas las membresías de una app de cliente.
   * @param clientAppId the client app ID
   * @return list of memberships
   */
  List<Membership> findByClientAppId(UUID clientAppId);

  /**
   * Check if a membership exists for the given user and client app.
   * <p>Verifica si existe una membresía para el usuario y app dados.
   * @param userId the user ID
   * @param clientAppId the client app ID
   * @return true if membership exists
   */
  boolean existsByUserAndClientApp(UUID userId, UUID clientAppId);

  /**
   * Persist a new membership.
   * <p>Persiste una nueva membresía.
   * @param membership the membership to save
   * @return the persisted membership
   */
  Membership save(Membership membership);

  /**
   * Update an existing membership.
   * <p>Actualiza una membresía existente.
   * @param membership the membership to update
   * @return the updated membership
   */
  Membership update(Membership membership);

  /**
   * Delete a membership by ID.
   * <p>Elimina una membresía por ID.
   * @param membershipId the ID of the membership to delete
   */
  void deleteById(MembershipId membershipId);

  /**
   * Find the role codes of the active membership for a given user and client app.
   * <p>Retorna los códigos de rol de la membresía activa del usuario en la app indicada.
   * Returns an empty list if the user has no active membership or no roles assigned.
   *
   * @param userId      the tenant user ID
   * @param clientAppId the client app ID
   * @return list of role code strings (e.g. ["ADMIN", "USER_TENANT"])
   */
  List<String> findRoleCodesByUserAndClientApp(UUID userId, UUID clientAppId);
}

