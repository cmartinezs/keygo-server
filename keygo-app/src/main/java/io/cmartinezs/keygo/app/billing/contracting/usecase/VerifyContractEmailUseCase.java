package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Use case: verify the email code of a contract — billing model v2.
 * <ol>
 *   <li>Validates the 6-digit code.</li>
 *   <li>Finds or creates a TenantUser in the provider's tenant using the contractor email.</li>
 *   <li>Finds or creates a Contractor linked to that TenantUser.</li>
 *   <li>Links the Contractor to the contract and advances status to PENDING_PAYMENT.</li>
 * </ol>
 *
 * @author cmartinezs
 * @version 1.0
 */
public class VerifyContractEmailUseCase {

  private final AppContractRepositoryPort contractRepo;
  private final ClientAppRepositoryPort clientAppRepo;
  private final UserRepositoryPort userRepo;
  private final ContractorRepositoryPort contractorRepo;

  public VerifyContractEmailUseCase(
      AppContractRepositoryPort contractRepo,
      ClientAppRepositoryPort clientAppRepo,
      UserRepositoryPort userRepo,
      ContractorRepositoryPort contractorRepo) {
    this.contractRepo = contractRepo;
    this.clientAppRepo = clientAppRepo;
    this.userRepo = userRepo;
    this.contractorRepo = contractorRepo;
  }

  public AppContractResult execute(UUID contractId, String inputCode) {
    var contract = contractRepo.findById(contractId)
        .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado: " + contractId));

    OffsetDateTime now = OffsetDateTime.now();

    // Extract fields before lambdas (contract gets reassigned later → not effectively final)
    final UUID clientAppId = contract.getClientAppId();
    final String contractorEmail    = contract.getContractorEmail();
    final String contractorFirst    = contract.getContractorFirstName();
    final String contractorLast     = contract.getContractorLastName();
    final String generatedUsername  = contract.generateUsername();

    // Resolve provider tenant from the contract's client app
    var providerApp = clientAppRepo.findById(ClientAppId.of(clientAppId))
        .orElseThrow(() -> new IllegalStateException(
            "ClientApp del proveedor no encontrada: " + clientAppId));

    // Find or create TenantUser in the provider's tenant
    final EmailAddress email = EmailAddress.of(contractorEmail);
    final var tenantId = providerApp.getTenantId();

    User tenantUser = userRepo.findByTenantIdAndEmail(tenantId, email)
        .orElseGet(() -> {
          User newUser = User.builder()
              .id(UserId.of(UUID.randomUUID()))
              .tenantId(tenantId)
              .email(email)
              .username(Username.of(generatedUsername))
              .firstName(contractorFirst)
              .lastName(contractorLast)
              .status(UserStatus.ACTIVE)
              .build();
          return userRepo.save(newUser);
        });

    // Find or create Contractor linked to this TenantUser
    final UUID tenantUserIdFinal = tenantUser.getId().value();
    Contractor contractor = contractorRepo.findByTenantUserId(tenantUserIdFinal)
        .orElseGet(() -> {
          Contractor newContractor = Contractor.builder()
              .id(UUID.randomUUID())
              .tenantUserId(tenantUserIdFinal)
              .status(ContractorStatus.PENDING)
              .build();
          return contractorRepo.save(newContractor);
        });

    // Verify code and link contractor — advances status to PENDING_PAYMENT
    contract.verifyCode(inputCode, contractor.getId(), now);
    contract = contractRepo.save(contract);

    return new AppContractResult(contract, null);
  }
}
