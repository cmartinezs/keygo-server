package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.result.PlatformRoleContractorResult;
import io.cmartinezs.keygo.app.membership.result.PlatformUserRoleResult;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.PlatformRole;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRole;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Use case: list platform roles assigned to a global platform user.
 * <p>Caso de uso: listar los roles de plataforma asignados a un usuario global.
 */
public class ListPlatformUserRolesUseCase {

  private final PlatformUserRepositoryPort platformUserRepositoryPort;
  private final PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort;
  private final PlatformRoleRepositoryPort platformRoleRepositoryPort;
  private final ContractorRepositoryPort contractorRepositoryPort;

  public ListPlatformUserRolesUseCase(
      PlatformUserRepositoryPort platformUserRepositoryPort,
      PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort,
      PlatformRoleRepositoryPort platformRoleRepositoryPort,
      ContractorRepositoryPort contractorRepositoryPort) {
    this.platformUserRepositoryPort = platformUserRepositoryPort;
    this.platformUserRoleRepositoryPort = platformUserRoleRepositoryPort;
    this.platformRoleRepositoryPort = platformRoleRepositoryPort;
    this.contractorRepositoryPort = contractorRepositoryPort;
  }

  public List<PlatformUserRoleResult> execute(UUID platformUserId) {
    UserId userId = UserId.of(platformUserId);
    platformUserRepositoryPort.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("id", platformUserId.toString()));

    List<PlatformUserRole> assignments =
        platformUserRoleRepositoryPort.findByPlatformUserId(platformUserId);

    Map<UUID, Contractor> contractorsById =
        assignments.stream()
            .map(PlatformUserRole::getContractorId)
            .flatMap(Optional::stream)
            .distinct()
            .collect(Collectors.toMap(Function.identity(), this::resolveContractor));

    Map<UUID, PlatformRole> rolesById =
        platformRoleRepositoryPort.findAll().stream()
            .collect(Collectors.toMap(role -> role.getId().value(), Function.identity()));

    return assignments.stream()
        .map(assignment -> toResult(assignment, rolesById, contractorsById))
        .toList();
  }

  private PlatformUserRoleResult toResult(
      PlatformUserRole assignment,
      Map<UUID, PlatformRole> rolesById,
      Map<UUID, Contractor> contractorsById) {
    UUID roleId = assignment.getPlatformRoleId().value();
    PlatformRole role = rolesById.get(roleId);
    if (role == null) {
      throw new IllegalStateException(
          "Platform role metadata not found for assignment roleId=" + roleId);
    }

    Contractor contractor =
        assignment.getContractorId().map(contractorsById::get).orElse(null);

    return new PlatformUserRoleResult(
        assignment.getId().value(),
        roleId,
        role.getCode(),
        role.getName(),
        role.getDescription(),
        assignment.getScopeType(),
        assignment.getContractorId().orElse(null),
        assignment.getTenantId().orElse(null),
        contractor != null
            ? new PlatformRoleContractorResult(
                contractor.getId(), contractor.getDisplayName(), contractor.getBillingEmail())
            : null,
        assignment.getAssignedAt());
  }

  private Contractor resolveContractor(UUID contractorId) {
    return contractorRepositoryPort.findById(contractorId)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Contractor metadata not found for scoped assignment contractorId="
                        + contractorId));
  }
}
