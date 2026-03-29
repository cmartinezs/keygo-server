package io.cmartinezs.keygo.api.billing.controller;

import io.cmartinezs.keygo.app.billing.invoice.usecase.ListAppInvoicesUseCase;
import io.cmartinezs.keygo.app.billing.subscription.usecase.CancelAppSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.subscription.usecase.GetAppSubscriptionUseCase;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriptionStatus;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppBillingSubscriptionControllerTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID   = "acme-platform";

  @Mock TenantRepositoryPort tenantRepo;
  @Mock ClientAppRepositoryPort clientAppRepo;
  @Mock GetAppSubscriptionUseCase getSubscriptionUseCase;
  @Mock CancelAppSubscriptionUseCase cancelSubscriptionUseCase;
  @Mock ListAppInvoicesUseCase listInvoicesUseCase;

  @InjectMocks
  AppBillingSubscriptionController controller;

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

  private AppSubscription activeSubscription(UUID appId, UUID tenantId) {
    return AppSubscription.builder()
        .id(UUID.randomUUID())
        .clientAppId(appId)
        .appPlanVersionId(UUID.randomUUID())
        .subscriberTenantId(tenantId)
        .status(SubscriptionStatus.ACTIVE)
        .currentPeriodStart(OffsetDateTime.now().minusDays(1))
        .currentPeriodEnd(OffsetDateTime.now().plusMonths(1))
        .autoRenew(true)
        .build();
  }

  private void stubResolvers(Tenant t, ClientApp app) {
    // {tenantSlug} → suscriptor; {clientId} → proveedor (global, no bajo el tenant del suscriptor)
    when(tenantRepo.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(t));
    when(clientAppRepo.findByClientId(ClientId.of(CLIENT_ID))).thenReturn(Optional.of(app));
  }

  @Test
  void getSubscription_active_returns200() {
    // Given
    Tenant t = tenant();
    ClientApp app = clientApp(t.getId());
    stubResolvers(t, app);
    AppSubscription sub = activeSubscription(app.getId().value(), t.getId().value());
    when(getSubscriptionUseCase.executeForTenant(any(), any())).thenReturn(sub);

    // When
    var response = controller.getSubscription(TENANT_SLUG, CLIENT_ID);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().status()).isEqualTo("ACTIVE");
  }

  @Test
  void cancelSubscription_returns200WithCancelAtPeriodEndFlag() {
    // Given
    Tenant t = tenant();
    ClientApp app = clientApp(t.getId());
    stubResolvers(t, app);
    AppSubscription sub = AppSubscription.builder()
        .id(UUID.randomUUID())
        .clientAppId(app.getId().value())
        .appPlanVersionId(UUID.randomUUID())
        .subscriberTenantId(t.getId().value())
        .status(SubscriptionStatus.ACTIVE)
        .currentPeriodStart(OffsetDateTime.now().minusDays(1))
        .currentPeriodEnd(OffsetDateTime.now().plusMonths(1))
        .cancelAtPeriodEnd(true)
        .autoRenew(false)
        .build();
    when(cancelSubscriptionUseCase.executeForTenant(any(), any())).thenReturn(sub);

    // When
    var response = controller.cancelSubscription(TENANT_SLUG, CLIENT_ID);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().cancelAtPeriodEnd()).isTrue();
  }

  @Test
  void listInvoices_emptyList_returns200WithEmptyArray() {
    // Given
    Tenant t = tenant();
    ClientApp app = clientApp(t.getId());
    stubResolvers(t, app);
    AppSubscription sub = activeSubscription(app.getId().value(), t.getId().value());
    when(getSubscriptionUseCase.executeForTenant(any(), any())).thenReturn(sub);
    when(listInvoicesUseCase.execute(sub.getId())).thenReturn(List.of());

    // When
    var response = controller.listInvoices(TENANT_SLUG, CLIENT_ID);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData()).isEmpty();
  }
}



