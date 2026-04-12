package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.exception.ContractInvalidStateException;
import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.ContractEmailVerificationRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractEmailVerification;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Use case: resend the email verification code for a billing contract.
 * <ul>
 *   <li>If the current code is still valid → resends the <strong>same</strong> code (no regeneration,
 *       no error — the user simply did not receive the email).</li>
 *   <li>If the code has expired → generates a new code, updates the contract and sends the email.</li>
 * </ul>
 * <p>This policy is intentionally more permissive than the user-registration resend flow because
 * the contracting process is a critical business action where blocking a valid-code resend would
 * be detrimental to conversion.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ResendContractVerificationUseCase {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final AppContractRepositoryPort contractRepo;
  private final ContractEmailVerificationRepositoryPort contractVerificationRepo;
  private final EmailNotificationPort emailNotification;
  private final int verificationCodeExpiryMinutes;

  public ResendContractVerificationUseCase(
      AppContractRepositoryPort contractRepo,
      ContractEmailVerificationRepositoryPort contractVerificationRepo,
      EmailNotificationPort emailNotification,
      int verificationCodeExpiryMinutes) {
    this.contractRepo = contractRepo;
    this.contractVerificationRepo = contractVerificationRepo;
    this.emailNotification = emailNotification;
    this.verificationCodeExpiryMinutes = verificationCodeExpiryMinutes;
  }

  /**
   * Executes the resend flow.
   *
   * @param contractId the contract UUID
   * @return {@link AppContractResult} with the (possibly updated) contract
   * @throws ContractNotFoundException     if the contract does not exist
   * @throws ContractInvalidStateException if the contract is not in PENDING_EMAIL_VERIFICATION state
   */
  public AppContractResult execute(UUID contractId) {
    var contract = contractRepo.findById(contractId)
        .orElseThrow(() -> new ContractNotFoundException(contractId));

    if (!ContractStatus.PENDING_EMAIL_VERIFICATION.equals(contract.getStatus())) {
      throw new ContractInvalidStateException(contractId, contract.getStatus(),
          "resend only applies in PENDING_EMAIL_VERIFICATION state");
    }

    OffsetDateTime now = OffsetDateTime.now();
    ContractEmailVerification verification = contractVerificationRepo.findByContractId(contractId)
        .orElse(null);

    if (verification == null) {
      verification = contractVerificationRepo.upsert(
          ContractEmailVerification.builder()
              .contractId(contractId)
              .code(String.format("%06d", RANDOM.nextInt(1_000_000)))
              .expiresAt(now.plusMinutes(verificationCodeExpiryMinutes))
              .build());
    } else if (verification.isExpired(now)) {
      verification.renew(
          String.format("%06d", RANDOM.nextInt(1_000_000)),
          now.plusMinutes(verificationCodeExpiryMinutes),
          now);
      verification = contractVerificationRepo.upsert(verification);
    }

    String recipientName = contract.getContractorFirstName() + " " + contract.getContractorLastName();
    emailNotification.sendEmail(
        EmailNotificationPort.TYPE_CONTRACT_VERIFICATION,
        contract.getContractorEmail(), recipientName,
        Map.of("userUsername", contract.generateUsername(),
            "userFirstName", contract.getContractorFirstName() != null ? contract.getContractorFirstName() : "",
            "userLastName", contract.getContractorLastName() != null ? contract.getContractorLastName() : "",
            "verificationCode", verification.getCode(),
            "contract_id", contract.getId().toString(),
            "resume", "1",
            "expiresInMinutes", verificationCodeExpiryMinutes));

    return new AppContractResult(contract, null);
  }
}

