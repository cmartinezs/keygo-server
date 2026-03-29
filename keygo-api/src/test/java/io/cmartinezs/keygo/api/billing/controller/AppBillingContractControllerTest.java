package io.cmartinezs.keygo.api.billing.controller;

import io.cmartinezs.keygo.api.billing.request.CreateAppContractRequest;
import io.cmartinezs.keygo.api.billing.request.VerifyContractEmailRequest;
import io.cmartinezs.keygo.app.billing.contracting.usecase.VerifyContractEmailUseCase;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ActivateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.CreateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.GetAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.MockApprovePaymentUseCase;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppBillingContractControllerTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID   = "acme-platform";

  @Mock TenantRepositoryPort tenantRepo;
  @Mock ClientAppRepositoryPort clientAppRepo;
  @Mock CreateAppContractUseCase createContractUseCase;
  @Mock GetAppContractUseCase getContractUseCase;
  @Mock MockApprovePaymentUseCase mockApprovePaymentUseCase;
  @Mock ActivateAppContractUseCase activateContractUseCase;
  @Mock VerifyContractEmailUseCase verifyContractEmailUseCase;

  @InjectMocks
  AppBillingContractController controller;

  private Tenant tenant() {
    return Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME").ownerEmail("admin@acme.com")
        .status(TenantStatus.ACTIVE).build();
  }

  private ClientApp clientApp(TenantId tenantId) {
    return ClientApp.builder()
        .id(ClientAppId.generate()).tenantId(tenantId)
        .clientId(ClientId.of(CLIENT_ID)).name("ACME Platform")
        .type(ClientType.PUBLIC)
        .accessPolicy(new io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy(
            java.util.Set.of(io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant.AUTHORIZATION_CODE),
            java.util.Set.of()))
        .status(io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus.ACTIVE).build();
  }

  private AppContract contract(ContractStatus status) {
    return AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .subscriberType(SubscriberType.TENANT)
        .status(status)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John").contractorLastName("Doe")
        .companySlug("acme")
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
        .build();
  }

  private void stubResolver(Tenant t, ClientApp app) {
    when(tenantRepo.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(t));
    when(clientAppRepo.findByClientIdAndTenantId(eq(ClientId.of(CLIENT_ID)), any())).thenReturn(Optional.of(app));
  }

  @Test
  void createContract_happyPath_returns201() {
    // Given
    Tenant t = tenant();
    ClientApp app = clientApp(t.getId());
    stubResolver(t, app);
    AppContract c = contract(ContractStatus.PENDING_EMAIL_VERIFICATION);
    when(createContractUseCase.execute(any())).thenReturn(new AppContractResult(c, null));

    CreateAppContractRequest request = new CreateAppContractRequest(
        UUID.randomUUID().toString(), BillingPeriod.MONTHLY, SubscriberType.TENANT,
        "admin@acme.com", "John", "Doe", "ACME Corp", "acme", "RFC123", "Calle 1");

    // When
    var response = controller.createContract(TENANT_SLUG, CLIENT_ID, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().getData().status()).isEqualTo("PENDING_EMAIL_VERIFICATION");
  }

  @Test
  void getContract_existing_returns200() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contract(ContractStatus.PENDING_PAYMENT);
    when(getContractUseCase.execute(contractId)).thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.getContract(TENANT_SLUG, CLIENT_ID, contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().status()).isEqualTo("PENDING_PAYMENT");
  }

  @Test
  void mockApprovePayment_enabled_returns200() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contract(ContractStatus.READY_TO_ACTIVATE);
    when(mockApprovePaymentUseCase.isMockEnabled()).thenReturn(true);
    when(mockApprovePaymentUseCase.execute(contractId)).thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.mockApprovePayment(TENANT_SLUG, CLIENT_ID, contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().status()).isEqualTo("READY_TO_ACTIVATE");
  }

  @Test
  void mockApprovePayment_disabled_returns404() {
    // Given
    UUID contractId = UUID.randomUUID();
    when(mockApprovePaymentUseCase.isMockEnabled()).thenReturn(false);

    // When
    var response = controller.mockApprovePayment(TENANT_SLUG, CLIENT_ID, contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    verify(mockApprovePaymentUseCase, never()).execute(any());
  }

  @Test
  void activateContract_happyPath_returns200WithActivatedStatus() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contract(ContractStatus.ACTIVATED);
    when(activateContractUseCase.execute(contractId)).thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.activateContract(TENANT_SLUG, CLIENT_ID, contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().status()).isEqualTo("ACTIVATED");
  }

  @Test
  void verifyEmail_validCode_returns200WithPendingPaymentStatus() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contract(ContractStatus.PENDING_PAYMENT);
    when(verifyContractEmailUseCase.execute(contractId, "123456"))
        .thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.verifyEmail(TENANT_SLUG, CLIENT_ID, contractId,
        new VerifyContractEmailRequest("123456"));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().status()).isEqualTo("PENDING_PAYMENT");
  }
}
