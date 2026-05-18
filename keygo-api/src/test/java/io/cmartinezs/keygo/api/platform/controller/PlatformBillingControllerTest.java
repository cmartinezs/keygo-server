package io.cmartinezs.keygo.api.platform.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.api.billing.response.AppInvoiceData;
import io.cmartinezs.keygo.api.billing.response.AppPlanData;
import io.cmartinezs.keygo.api.billing.response.AppSubscriptionData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.app.billing.platform.exception.ContractorNotFoundException;
import io.cmartinezs.keygo.app.billing.platform.exception.PlanNotFoundException;
import io.cmartinezs.keygo.app.billing.platform.usecase.CancelPlatformSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.GetPlatformPlanCatalogUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.GetPlatformPlanUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.GetPlatformSubscriptionUseCase;
import io.cmartinezs.keygo.app.billing.platform.usecase.ListPlatformInvoicesUseCase;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanStatus;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersionStatus;
import io.cmartinezs.keygo.domain.billing.invoice.model.Invoice;
import io.cmartinezs.keygo.domain.billing.invoice.model.InvoiceStatus;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriptionStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformBillingController")
class PlatformBillingControllerTest {

  @Mock private GetPlatformPlanCatalogUseCase getPlanCatalogUseCase;
  @Mock private GetPlatformPlanUseCase getPlanUseCase;
  @Mock private GetPlatformSubscriptionUseCase getSubscriptionUseCase;
  @Mock private CancelPlatformSubscriptionUseCase cancelSubscriptionUseCase;
  @Mock private ListPlatformInvoicesUseCase listInvoicesUseCase;

  private PlatformBillingController controller;

  private static final UUID PLATFORM_USER_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    controller = new PlatformBillingController(
        getPlanCatalogUseCase,
        getPlanUseCase,
        getSubscriptionUseCase,
        cancelSubscriptionUseCase,
        listInvoicesUseCase);
  }

  private Authentication authenticatedUser() {
    return new UsernamePasswordAuthenticationToken(
        PLATFORM_USER_ID.toString(), null,
        List.of(new SimpleGrantedAuthority("ROLE_KEYGO_ACCOUNT_ADMIN")));
  }

  // ── Catalog ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /platform/billing/catalog")
  class CatalogTests {

    @Test
    @DisplayName("Debe retornar 200 con lista de planes de plataforma incluyendo versions y entitlements")
    void getCatalog_returns200WithPlans() {
      // Given
      var planId = UUID.randomUUID();
      var versionId = UUID.randomUUID();
      AppPlan plan = AppPlan.builder()
          .id(planId).code("FREE").name("Free")
          .status(AppPlanStatus.ACTIVE).isPublic(true).sortOrder(1).build();
      AppPlanVersion version = AppPlanVersion.builder()
          .id(versionId).appPlanId(planId).version("v1.0")
          .currency("USD").setupFee(BigDecimal.ZERO).trialDays(0)
          .effectiveFrom(LocalDate.now()).status(AppPlanVersionStatus.ACTIVE).build();
      var result = new AppPlanResult(plan, List.of(version), Map.of(versionId, List.of()), List.of());
      when(getPlanCatalogUseCase.execute()).thenReturn(List.of(result));

      // When
      ResponseEntity<BaseResponse<List<AppPlanData>>> response = controller.getCatalog();

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getData()).hasSize(1);
      assertThat(response.getBody().getData().getFirst().versions()).hasSize(1);
      assertThat(response.getBody().getSuccess().getCode())
          .isEqualTo(ResponseCode.PLATFORM_PLAN_CATALOG_RETRIEVED.getCode());
      verify(getPlanCatalogUseCase).execute();
    }

    @Test
    @DisplayName("Debe retornar 200 con lista vacía si no hay planes")
    void getCatalog_returns200WithEmptyList() {
      // Given
      when(getPlanCatalogUseCase.execute()).thenReturn(List.of());

      // When
      ResponseEntity<BaseResponse<List<AppPlanData>>> response = controller.getCatalog();

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getData()).isEmpty();
    }
  }

  // ── Plan detail ────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /platform/billing/catalog/{planCode}")
  class PlanDetailTests {

    @Test
    @DisplayName("Debe retornar 200 con detalle de plan incluyendo versions")
    void getPlan_returns200WithPlanDetail() {
      // Given
      String planCode = "PERSONAL";
      var planId = UUID.randomUUID();
      AppPlan plan = AppPlan.builder()
          .id(planId).code(planCode).name("Personal")
          .status(AppPlanStatus.ACTIVE).isPublic(true).sortOrder(2).build();
      var result = new AppPlanResult(plan, List.of(), Map.of(), List.of());
      when(getPlanUseCase.execute(planCode)).thenReturn(result);

      // When
      ResponseEntity<BaseResponse<AppPlanData>> response = controller.getPlan(planCode);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getData().code()).isEqualTo(planCode);
      assertThat(response.getBody().getSuccess().getCode())
          .isEqualTo(ResponseCode.PLATFORM_PLAN_RETRIEVED.getCode());
    }

    @Test
    @DisplayName("Debe propagar PlanNotFoundException al handler global")
    void getPlan_throwsWhenNotFound() {
      // Given
      String planCode = "NONEXISTENT";
      when(getPlanUseCase.execute(planCode)).thenThrow(new PlanNotFoundException(planCode));

      // When / Then — exception propagates to GlobalExceptionHandler
      org.junit.jupiter.api.Assertions.assertThrows(
          PlanNotFoundException.class, () -> controller.getPlan(planCode));
    }
  }

  // ── Subscription ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /platform/billing/subscription")
  class SubscriptionTests {

    @Test
    @DisplayName("Debe retornar 200 con suscripción activa")
    void getSubscription_returns200WithActiveSubscription() {
      // Given
      AppSubscription subscription = AppSubscription.builder()
          .id(UUID.randomUUID()).contractorId(UUID.randomUUID())
          .appPlanVersionId(UUID.randomUUID())
          .status(SubscriptionStatus.ACTIVE)
          .currentPeriodStart(OffsetDateTime.now().minusDays(15))
          .currentPeriodEnd(OffsetDateTime.now().plusDays(15))
          .autoRenew(true)
          .createdAt(OffsetDateTime.now()).build();
      when(getSubscriptionUseCase.execute(PLATFORM_USER_ID)).thenReturn(subscription);

      // When
      ResponseEntity<BaseResponse<AppSubscriptionData>> response =
          controller.getSubscription(authenticatedUser());

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getSuccess().getCode())
          .isEqualTo(ResponseCode.PLATFORM_SUBSCRIPTION_RETRIEVED.getCode());
    }

    @Test
    @DisplayName("Debe propagar ContractorNotFoundException al handler global")
    void getSubscription_throwsWhenContractorNotFound() {
      // Given
      when(getSubscriptionUseCase.execute(PLATFORM_USER_ID))
          .thenThrow(new ContractorNotFoundException(PLATFORM_USER_ID));

      // When / Then
      org.junit.jupiter.api.Assertions.assertThrows(
          ContractorNotFoundException.class,
          () -> controller.getSubscription(authenticatedUser()));
    }
  }

  // ── Cancel subscription ────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /platform/billing/subscription/cancel")
  class CancelTests {

    @Test
    @DisplayName("Debe retornar 200 con suscripción marcada para cancelación")
    void cancelSubscription_returns200() {
      // Given
      AppSubscription subscription = AppSubscription.builder()
          .id(UUID.randomUUID()).contractorId(UUID.randomUUID())
          .appPlanVersionId(UUID.randomUUID())
          .status(SubscriptionStatus.ACTIVE)
          .cancelAtPeriodEnd(true)
          .cancelledAt(OffsetDateTime.now())
          .currentPeriodStart(OffsetDateTime.now().minusDays(15))
          .currentPeriodEnd(OffsetDateTime.now().plusDays(15))
          .autoRenew(true)
          .createdAt(OffsetDateTime.now()).build();
      when(cancelSubscriptionUseCase.execute(PLATFORM_USER_ID)).thenReturn(subscription);

      // When
      ResponseEntity<BaseResponse<AppSubscriptionData>> response =
          controller.cancelSubscription(authenticatedUser());

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getSuccess().getCode())
          .isEqualTo(ResponseCode.PLATFORM_SUBSCRIPTION_CANCELLED.getCode());
    }
  }

  // ── Invoices ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /platform/billing/invoices")
  class InvoiceTests {

    @Test
    @DisplayName("Debe retornar 200 con lista de facturas")
    void listInvoices_returns200WithInvoices() {
      // Given
      Invoice invoice = Invoice.builder()
          .id(UUID.randomUUID()).subscriptionId(UUID.randomUUID())
          .invoiceNumber("INV-001")
          .status(InvoiceStatus.PAID)
          .issueDate(LocalDate.now().minusDays(30))
          .dueDate(LocalDate.now().minusDays(15))
          .periodStart(LocalDate.now().minusDays(30))
          .periodEnd(LocalDate.now())
          .currency("USD")
          .subtotal(BigDecimal.valueOf(9.99))
          .taxAmount(BigDecimal.ZERO)
          .total(BigDecimal.valueOf(9.99))
          .createdAt(OffsetDateTime.now()).build();
      when(listInvoicesUseCase.execute(PLATFORM_USER_ID)).thenReturn(List.of(invoice));

      // When
      ResponseEntity<BaseResponse<List<AppInvoiceData>>> response =
          controller.listInvoices(authenticatedUser());

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getData()).hasSize(1);
      assertThat(response.getBody().getSuccess().getCode())
          .isEqualTo(ResponseCode.PLATFORM_INVOICES_RETRIEVED.getCode());
    }

    @Test
    @DisplayName("Debe retornar 200 con lista vacía si no hay facturas")
    void listInvoices_returns200WithEmptyList() {
      // Given
      when(listInvoicesUseCase.execute(PLATFORM_USER_ID)).thenReturn(List.of());

      // When
      ResponseEntity<BaseResponse<List<AppInvoiceData>>> response =
          controller.listInvoices(authenticatedUser());

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      assertThat(response.getBody().getData()).isEmpty();
    }
  }
}
