package io.cmartinezs.keygo.app.billing.contracting.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.command.CreateAppContractCommand;
import io.cmartinezs.keygo.app.billing.contracting.exception.ContractorEmailAlreadyExistsException;
import io.cmartinezs.keygo.app.billing.contracting.exception.PlanVersionNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for CreateAppContractUseCase — email duplication validation.
 *
 * @author cmartinezs
 * @version 1.1
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateAppContractUseCase — Validación de email duplicado")
class CreateAppContractUseCaseTest {

  @Mock private AppContractRepositoryPort contractRepo;
  @Mock private AppPlanVersionRepositoryPort versionRepo;
  @Mock private ClientAppRepositoryPort clientAppRepo;
  @Mock private ContractorRepositoryPort contractorRepo;
  @Mock private EmailNotificationPort emailNotification;

  private CreateAppContractUseCase useCase;

  private static final UUID CLIENT_APP_ID = UUID.randomUUID();
  private static final UUID PLAN_VERSION_ID = UUID.randomUUID();
  private static final UUID PROVIDER_TENANT_ID = UUID.randomUUID();
  private static final String CONTRACTOR_EMAIL = "contractor@example.com";
  private static final String CONTRACTOR_FIRST_NAME = "John";
  private static final String CONTRACTOR_LAST_NAME = "Doe";
  private static final int CONTRACT_EXPIRY_HOURS = 72;
  private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 30;

  @BeforeEach
  void setUp() {
    useCase =
        new CreateAppContractUseCase(
            contractRepo,
            versionRepo,
            clientAppRepo,
            contractorRepo,
            emailNotification,
            CONTRACT_EXPIRY_HOURS,
            VERIFICATION_CODE_EXPIRY_MINUTES);
  }

  @Test
  @DisplayName("Debe crear contrato cuando el email NO existe como contractor")
  void shouldCreateContractWhenEmailDoesNotExist() {
    // Given
    CreateAppContractCommand cmd =
        new CreateAppContractCommand(
            CLIENT_APP_ID,
            PLAN_VERSION_ID,
            BillingPeriod.MONTHLY,
            CONTRACTOR_EMAIL,
            CONTRACTOR_FIRST_NAME,
            CONTRACTOR_LAST_NAME,
            "Acme Corp",
            "12345678-9",
            "123 Main St");

    // Mock plan version exists
    when(versionRepo.findById(PLAN_VERSION_ID)).thenReturn(Optional.of(mock(AppPlanVersion.class)));

    // Mock client app exists (validates when clientAppId != null)
    ClientApp clientApp =
        ClientApp.builder()
            .id(ClientAppId.of(CLIENT_APP_ID))
            .tenantId(TenantId.of(PROVIDER_TENANT_ID))
            .name("Test App")
            .clientId(ClientId.of("test-app"))
            .status(ClientAppStatus.ACTIVE)
            .type(ClientType.PUBLIC)
            .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
            .build();
    when(clientAppRepo.findById(ClientAppId.of(CLIENT_APP_ID))).thenReturn(Optional.of(clientApp));

    // Mock contractor does NOT exist (email available — platform-level check)
    when(contractorRepo.findByPlatformUserEmail(CONTRACTOR_EMAIL))
        .thenReturn(Optional.empty());

    // Mock contract save
    AppContract savedContract =
        AppContract.builder()
            .id(UUID.randomUUID())
            .clientAppId(CLIENT_APP_ID)
            .selectedPlanVersionId(PLAN_VERSION_ID)
            .billingPeriod("MONTHLY")
            .status(ContractStatus.PENDING_EMAIL_VERIFICATION)
            .contractorEmail(CONTRACTOR_EMAIL)
            .contractorFirstName(CONTRACTOR_FIRST_NAME)
            .contractorLastName(CONTRACTOR_LAST_NAME)
            .companyName("Acme Corp")
            .companyTaxId("12345678-9")
            .companyAddress("123 Main St")
            .verificationCode("123456")
            .verificationCodeExpiresAt(
                OffsetDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
            .expiresAt(OffsetDateTime.now().plusHours(CONTRACT_EXPIRY_HOURS))
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();
    when(contractRepo.save(any(AppContract.class))).thenReturn(savedContract);

    // When
    AppContractResult result = useCase.execute(cmd);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.contract()).isNotNull();
    assertThat(result.contract().getContractorEmail()).isEqualTo(CONTRACTOR_EMAIL);
    assertThat(result.contract().getStatus()).isEqualTo(ContractStatus.PENDING_EMAIL_VERIFICATION);

    verify(versionRepo).findById(PLAN_VERSION_ID);
    verify(clientAppRepo).findById(ClientAppId.of(CLIENT_APP_ID));
    verify(contractorRepo).findByPlatformUserEmail(CONTRACTOR_EMAIL);
    verify(contractRepo).save(any(AppContract.class));
    verify(emailNotification)
        .sendContractVerificationEmail(
            eq(CONTRACTOR_EMAIL),
            eq(CONTRACTOR_FIRST_NAME + " " + CONTRACTOR_LAST_NAME),
            anyString(),
            any(UUID.class));
  }

  @Test
  @DisplayName("Debe rechazar creación cuando el email YA existe como contractor")
  void shouldRejectContractWhenEmailAlreadyExists() {
    // Given
    CreateAppContractCommand cmd =
        new CreateAppContractCommand(
            CLIENT_APP_ID,
            PLAN_VERSION_ID,
            BillingPeriod.MONTHLY,
            CONTRACTOR_EMAIL,
            CONTRACTOR_FIRST_NAME,
            CONTRACTOR_LAST_NAME,
            null,
            null,
            null);

    // Mock plan version exists
    when(versionRepo.findById(PLAN_VERSION_ID)).thenReturn(Optional.of(mock(AppPlanVersion.class)));

    // Mock client app exists
    ClientApp clientApp =
        ClientApp.builder()
            .id(ClientAppId.of(CLIENT_APP_ID))
            .tenantId(TenantId.of(PROVIDER_TENANT_ID))
            .name("Test App")
            .clientId(ClientId.of("test-app"))
            .status(ClientAppStatus.ACTIVE)
            .type(ClientType.PUBLIC)
            .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
            .build();
    when(clientAppRepo.findById(ClientAppId.of(CLIENT_APP_ID))).thenReturn(Optional.of(clientApp));

    // Mock contractor ALREADY EXISTS (email taken — platform-level check)
    Contractor existingContractor =
        Contractor.builder()
            .id(UUID.randomUUID())
            .platformUserId(UUID.randomUUID())
            .status(ContractorStatus.ACTIVE)
            .build();
    when(contractorRepo.findByPlatformUserEmail(CONTRACTOR_EMAIL))
        .thenReturn(Optional.of(existingContractor));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(cmd))
        .isInstanceOf(ContractorEmailAlreadyExistsException.class)
        .hasMessageContaining(CONTRACTOR_EMAIL);

    verify(versionRepo).findById(PLAN_VERSION_ID);
    verify(clientAppRepo).findById(ClientAppId.of(CLIENT_APP_ID));
    verify(contractorRepo).findByPlatformUserEmail(CONTRACTOR_EMAIL);
    verify(contractRepo, never()).save(any());
    verify(emailNotification, never()).sendContractVerificationEmail(any(), any(), any(), any());
  }

  @Test
  @DisplayName("Debe rechazar si la versión del plan no existe")
  void shouldRejectWhenPlanVersionNotFound() {
    // Given
    CreateAppContractCommand cmd =
        new CreateAppContractCommand(
            CLIENT_APP_ID,
            PLAN_VERSION_ID,
            BillingPeriod.MONTHLY,
            CONTRACTOR_EMAIL,
            CONTRACTOR_FIRST_NAME,
            CONTRACTOR_LAST_NAME,
            null,
            null,
            null);

    // Mock plan version NOT found
    when(versionRepo.findById(PLAN_VERSION_ID)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(cmd))
        .isInstanceOf(PlanVersionNotFoundException.class)
        .hasMessageContaining(PLAN_VERSION_ID.toString());

    verify(versionRepo).findById(PLAN_VERSION_ID);
    verify(clientAppRepo, never()).findById(any());
    verify(contractorRepo, never()).findByPlatformUserEmail(any());
    verify(contractRepo, never()).save(any());
  }

  @Test
  @DisplayName("Debe rechazar si el client app no existe")
  void shouldRejectWhenClientAppNotFound() {
    // Given
    CreateAppContractCommand cmd =
        new CreateAppContractCommand(
            CLIENT_APP_ID,
            PLAN_VERSION_ID,
            BillingPeriod.MONTHLY,
            CONTRACTOR_EMAIL,
            CONTRACTOR_FIRST_NAME,
            CONTRACTOR_LAST_NAME,
            null,
            null,
            null);

    // Mock plan version exists
    when(versionRepo.findById(PLAN_VERSION_ID)).thenReturn(Optional.of(mock(AppPlanVersion.class)));

    // Mock client app NOT found
    when(clientAppRepo.findById(ClientAppId.of(CLIENT_APP_ID))).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(cmd))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Client app not found");

    verify(versionRepo).findById(PLAN_VERSION_ID);
    verify(clientAppRepo).findById(ClientAppId.of(CLIENT_APP_ID));
    verify(contractorRepo, never()).findByPlatformUserEmail(any());
    verify(contractRepo, never()).save(any());
  }

  @Test
  @DisplayName("Debe crear contrato de plataforma cuando clientAppId es null")
  void shouldCreatePlatformContractWhenClientAppIdIsNull() {
    // Given — platform contract (no clientAppId)
    CreateAppContractCommand cmd =
        new CreateAppContractCommand(
            null,
            PLAN_VERSION_ID,
            BillingPeriod.MONTHLY,
            CONTRACTOR_EMAIL,
            CONTRACTOR_FIRST_NAME,
            CONTRACTOR_LAST_NAME,
            null,
            null,
            null);

    when(versionRepo.findById(PLAN_VERSION_ID)).thenReturn(Optional.of(mock(AppPlanVersion.class)));
    when(contractorRepo.findByPlatformUserEmail(CONTRACTOR_EMAIL)).thenReturn(Optional.empty());

    AppContract savedContract =
        AppContract.builder()
            .id(UUID.randomUUID())
            .selectedPlanVersionId(PLAN_VERSION_ID)
            .billingPeriod("MONTHLY")
            .status(ContractStatus.PENDING_EMAIL_VERIFICATION)
            .contractorEmail(CONTRACTOR_EMAIL)
            .contractorFirstName(CONTRACTOR_FIRST_NAME)
            .contractorLastName(CONTRACTOR_LAST_NAME)
            .verificationCode("123456")
            .verificationCodeExpiresAt(OffsetDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
            .expiresAt(OffsetDateTime.now().plusHours(CONTRACT_EXPIRY_HOURS))
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();
    when(contractRepo.save(any(AppContract.class))).thenReturn(savedContract);

    // When
    AppContractResult result = useCase.execute(cmd);

    // Then — clientApp was NOT validated
    assertThat(result).isNotNull();
    assertThat(result.contract().getClientAppId()).isNull();
    verify(clientAppRepo, never()).findById(any());
    verify(contractorRepo).findByPlatformUserEmail(CONTRACTOR_EMAIL);
  }
}
