package io.cmartinezs.keygo.app.billing.contracting.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorUserRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorType;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorUserRole;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MockApprovePaymentUseCaseTest {

  @Mock AppContractRepositoryPort contractRepo;
  @Mock PlatformUserRepositoryPort platformUserRepo;
  @Mock PlatformUserRoleRepositoryPort platformUserRoleRepo;
  @Mock ContractorRepositoryPort contractorRepo;
  @Mock ContractorUserRepositoryPort contractorUserRepo;
  @Mock CredentialEncoderPort credentialEncoder;
  @Mock EmailNotificationPort emailNotification;

  private MockApprovePaymentUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new MockApprovePaymentUseCase(
        contractRepo,
        platformUserRepo,
        platformUserRoleRepo,
        contractorRepo,
        contractorUserRepo,
        credentialEncoder,
        emailNotification,
        true);
  }

  private AppContract pendingPaymentContract() {
    return AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .status(ContractStatus.PENDING_PAYMENT)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John")
        .contractorLastName("Doe")
        .emailVerifiedAt(OffsetDateTime.now().minusMinutes(5))
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
  }

  @Test
  void execute_newUser_provisionsAccountLinksContractorAndMovesToReadyToActivate() {
    AppContract contract = pendingPaymentContract();
    UUID platformUserId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(platformUserRepo.findByEmail(EmailAddress.of(contract.getContractorEmail())))
        .thenReturn(Optional.empty());
    when(credentialEncoder.encode(anyString())).thenReturn("$2a$10$hashed");
    when(platformUserRepo.save(any())).thenAnswer(inv -> {
      PlatformUser input = inv.getArgument(0);
      return PlatformUser.builder()
          .id(UserId.of(platformUserId))
          .username(input.getUsername())
          .email(input.getEmail())
          .passwordHash(PasswordHash.of("$2a$10$hashed"))
          .status(input.getStatus())
          .firstName(input.getFirstName())
          .lastName(input.getLastName())
          .build();
    });
    when(platformUserRoleRepo.hasRole(platformUserId, "keygo_user")).thenReturn(false);
    when(platformUserRoleRepo.hasRole(platformUserId, "keygo_tenant_admin")).thenReturn(false);
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.empty());
    when(contractorUserRepo.hasRole(contractorId, platformUserId, ContractorUserRole.OWNER)).thenReturn(false);
    when(contractorRepo.save(any())).thenReturn(Contractor.builder()
        .id(contractorId)
        .primaryContactPlatformUserId(platformUserId)
        .type(ContractorType.PERSON)
        .displayName("John Doe")
        .billingEmail(contract.getContractorEmail())
        .status(ContractorStatus.PENDING)
        .build());
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var result = useCase.execute(contract.getId());

    assertThat(result.contract().getStatus()).isEqualTo(ContractStatus.READY_TO_ACTIVATE);
    assertThat(result.contract().getContractorId()).isEqualTo(contractorId);
    verify(platformUserRepo).save(any());
    verify(platformUserRoleRepo).assign(platformUserId, "keygo_user");
    verify(platformUserRoleRepo).assign(platformUserId, "keygo_tenant_admin");
    verify(contractorUserRepo).assign(contractorId, platformUserId, ContractorUserRole.OWNER);
    verify(emailNotification).sendEmail(
        eq(EmailNotificationPort.TYPE_TEMPORARY_PASSWORD), anyString(), anyString(), any(Map.class));
  }

  @Test
  void execute_existingUser_reusesAccountWithoutSendingTemporaryPassword() {
    AppContract contract = pendingPaymentContract();
    UUID platformUserId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();
    PlatformUser existingUser = PlatformUser.builder()
        .id(UserId.of(platformUserId))
        .username(Username.of("jdoe"))
        .email(EmailAddress.of(contract.getContractorEmail()))
        .passwordHash(PasswordHash.of("$2a$10$hashed"))
        .status(UserStatus.ACTIVE)
        .firstName("John")
        .lastName("Doe")
        .build();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(platformUserRepo.findByEmail(EmailAddress.of(contract.getContractorEmail())))
        .thenReturn(Optional.of(existingUser));
    when(platformUserRoleRepo.hasRole(platformUserId, "keygo_user")).thenReturn(true);
    when(platformUserRoleRepo.hasRole(platformUserId, "keygo_tenant_admin")).thenReturn(true);
    when(contractorUserRepo.hasRole(contractorId, platformUserId, ContractorUserRole.OWNER)).thenReturn(true);
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.of(Contractor.builder()
        .id(contractorId)
        .primaryContactPlatformUserId(platformUserId)
        .type(ContractorType.PERSON)
        .displayName("John Doe")
        .billingEmail(contract.getContractorEmail())
        .status(ContractorStatus.ACTIVE)
        .build()));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var result = useCase.execute(contract.getId());

    assertThat(result.contract().getStatus()).isEqualTo(ContractStatus.READY_TO_ACTIVATE);
    assertThat(result.contract().getContractorId()).isEqualTo(contractorId);
    verify(platformUserRepo, never()).save(any());
    verify(emailNotification, never()).sendEmail(anyString(), anyString(), anyString(), any());
    verify(platformUserRoleRepo, never()).assign(any(), anyString());
    verify(contractorUserRepo, never()).assign(any(), any(), any());
  }
}
