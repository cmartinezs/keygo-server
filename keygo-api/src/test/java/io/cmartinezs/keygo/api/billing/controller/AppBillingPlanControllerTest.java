package io.cmartinezs.keygo.api.billing.controller;

import io.cmartinezs.keygo.api.billing.request.CreateAppPlanRequest;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.app.billing.catalog.usecase.CreateAppPlanUseCase;
import io.cmartinezs.keygo.app.billing.catalog.usecase.GetAppPlanCatalogUseCase;
import io.cmartinezs.keygo.app.billing.catalog.usecase.GetAppPlanUseCase;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanBillingOption;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanStatus;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersionStatus;
import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppBillingPlanControllerTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID   = "acme-platform";

  @Mock TenantRepositoryPort tenantRepo;
  @Mock ClientAppRepositoryPort clientAppRepo;
  @Mock GetAppPlanCatalogUseCase getCatalogUseCase;
  @Mock GetAppPlanUseCase getPlanUseCase;
  @Mock CreateAppPlanUseCase createPlanUseCase;

  @InjectMocks
  AppBillingPlanController controller;

  // ── fixtures ──────────────────────────────────────────────────────────────

  private Tenant tenant() {
    return Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private ClientApp clientApp(TenantId tenantId) {
    return ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(tenantId)
        .clientId(ClientId.of(CLIENT_ID))
        .name("ACME Platform")
        .type(ClientType.PUBLIC)
        .accessPolicy(new io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy(
            java.util.Set.of(io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant.AUTHORIZATION_CODE),
            java.util.Set.of()))
        .status(io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus.ACTIVE)
        .build();
  }

  private AppPlan plan(UUID appId) {
    return AppPlan.builder()
        .id(UUID.randomUUID())
        .clientAppId(appId)
        .code("STARTER")
        .name("Starter")
        .status(AppPlanStatus.ACTIVE)
        .isPublic(true)
        .sortOrder(1)
        .build();
  }

  /** Version without billing period/price (those go in billing options). */
  private AppPlanVersion version(UUID planId) {
    return AppPlanVersion.builder()
        .id(UUID.randomUUID()).appPlanId(planId).version("1.0")
        .currency("MXN").setupFee(BigDecimal.ZERO)
        .trialDays(14).effectiveFrom(LocalDate.now())
        .status(AppPlanVersionStatus.ACTIVE).build();
  }

  private AppPlanBillingOption monthlyOption(UUID versionId) {
    return AppPlanBillingOption.builder()
        .id(UUID.randomUUID()).appPlanVersionId(versionId)
        .billingPeriod(BillingPeriod.MONTHLY)
        .basePrice(new BigDecimal("299"))
        .discountPct(BigDecimal.ZERO)
        .isDefault(true).build();
  }

  private AppPlanResult planResult(AppPlan p) {
    AppPlanVersion v = version(p.getId());
    AppPlanBillingOption opt = monthlyOption(v.getId());
    return new AppPlanResult(p, List.of(v), Map.of(v.getId(), List.of(opt)), List.of());
  }

  private void stubResolver(Tenant t, ClientApp app) {
    when(tenantRepo.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(t));
    when(clientAppRepo.findByClientIdAndTenantId(eq(ClientId.of(CLIENT_ID)), any())).thenReturn(Optional.of(app));
  }

  // ── tests ─────────────────────────────────────────────────────────────────

  @Test
  void getCatalog_noFilter_returns200WithAllPlans() {
    // Given
    Tenant t = tenant();
    ClientApp app = clientApp(t.getId());
    AppPlan p = plan(app.getId().value());
    stubResolver(t, app);
    when(getCatalogUseCase.execute(any())).thenReturn(List.of(planResult(p)));

    // When
    var response = controller.getCatalog(TENANT_SLUG, CLIENT_ID);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData()).hasSize(1);
    assertThat(response.getBody().getData().get(0).code()).isEqualTo("STARTER");
    assertThat(response.getBody().getData().get(0).sortOrder()).isEqualTo(1);
  }

  @Test
  void getPlanPublic_existing_returns200() {
    // Given
    Tenant t = tenant();
    ClientApp app = clientApp(t.getId());
    AppPlan p = plan(app.getId().value());
    stubResolver(t, app);
    when(getPlanUseCase.execute(any(), eq("STARTER"))).thenReturn(planResult(p));

    // When
    var response = controller.getPlanPublic(TENANT_SLUG, CLIENT_ID, "STARTER");

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().code()).isEqualTo("STARTER");
    // Version should have 1 billing option (MONTHLY), not free
    assertThat(response.getBody().getData().versions().get(0).free()).isFalse();
    assertThat(response.getBody().getData().versions().get(0).billingOptions()).hasSize(1);
  }

  @Test
  void getPlanPublic_notFound_propagatesException() {
    // Given
    Tenant t = tenant();
    ClientApp app = clientApp(t.getId());
    stubResolver(t, app);
    when(getPlanUseCase.execute(any(), eq("MISSING")))
        .thenThrow(new ClientAppNotFoundException("MISSING"));

    // When / Then
    assertThatThrownBy(() -> controller.getPlanPublic(TENANT_SLUG, CLIENT_ID, "MISSING"))
        .isInstanceOf(ClientAppNotFoundException.class);
  }

  @Test
  void createPlan_happyPath_returns201() {
    // Given
    Tenant t = tenant();
    ClientApp app = clientApp(t.getId());
    AppPlan p = plan(app.getId().value());
    stubResolver(t, app);

    CreateAppPlanRequest request = new CreateAppPlanRequest(
        "STARTER", "Starter Plan", null, true,
        1,          // sortOrder
        "1.0", "MXN", 14, LocalDate.now(),
        List.of(new CreateAppPlanRequest.BillingOptionRequest(
            BillingPeriod.MONTHLY, new BigDecimal("299"), BigDecimal.ZERO, true)),
        List.of());
    when(createPlanUseCase.execute(any())).thenReturn(planResult(p));

    // When
    var response = controller.createPlan(TENANT_SLUG, CLIENT_ID, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().getData().code()).isEqualTo("STARTER");
  }

  @Test
  void getCatalog_unknownTenant_propagatesTenantNotFoundException() {
    // Given
    when(tenantRepo.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> controller.getCatalog(TENANT_SLUG, CLIENT_ID))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  void getCatalog_unknownClientApp_propagatesClientAppNotFoundException() {
    // Given
    Tenant t = tenant();
    when(tenantRepo.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(t));
    when(clientAppRepo.findByClientIdAndTenantId(any(), any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> controller.getCatalog(TENANT_SLUG, CLIENT_ID))
        .isInstanceOf(ClientAppNotFoundException.class);
  }
}

