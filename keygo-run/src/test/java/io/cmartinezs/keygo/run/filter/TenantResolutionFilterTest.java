package io.cmartinezs.keygo.run.filter;

import io.cmartinezs.keygo.app.tenant.usecase.GetTenantBySlugUseCase;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantResolutionFilter")
class TenantResolutionFilterTest {

  private static final String ACTIVE_SLUG = "my-tenant";
  private static final String SUSPENDED_SLUG = "suspended-org";
  private static final String UNKNOWN_SLUG = "unknown";

  @Mock
  private GetTenantBySlugUseCase getTenantBySlugUseCase;

  @Mock
  private FilterChain filterChain;

  private TenantResolutionFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    filter = new TenantResolutionFilter(getTenantBySlugUseCase);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  private Tenant activeTenant(String slug) {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(slug))
        .name("Test")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private Tenant suspendedTenant() {
    Tenant t = activeTenant(SUSPENDED_SLUG);
    t.suspend();
    return t;
  }

  @Test
  @DisplayName("should pass through when X-Tenant-Slug header is absent")
  void shouldPassThroughWithoutHeader() throws ServletException, IOException {
    // Given — no header

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
    verifyNoInteractions(getTenantBySlugUseCase);
  }

  @Test
  @DisplayName("should set tenant context and proceed when tenant is active")
  void shouldSetContextForActiveTenant() throws ServletException, IOException {
    // Given
    request.addHeader(TenantResolutionFilter.TENANT_SLUG_HEADER, ACTIVE_SLUG);
    when(getTenantBySlugUseCase.execute(ACTIVE_SLUG)).thenReturn(activeTenant(ACTIVE_SLUG));

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("should return 404 when tenant is not found")
  void shouldReturn404WhenTenantNotFound() throws ServletException, IOException {
    // Given
    request.addHeader(TenantResolutionFilter.TENANT_SLUG_HEADER, UNKNOWN_SLUG);
    when(getTenantBySlugUseCase.execute(UNKNOWN_SLUG)).thenThrow(new TenantNotFoundException(UNKNOWN_SLUG));

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    assertThat(response.getStatus()).isEqualTo(404);
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  @DisplayName("should return 403 when tenant is suspended")
  void shouldReturn403WhenTenantSuspended() throws ServletException, IOException {
    // Given
    request.addHeader(TenantResolutionFilter.TENANT_SLUG_HEADER, SUSPENDED_SLUG);
    when(getTenantBySlugUseCase.execute(SUSPENDED_SLUG)).thenReturn(suspendedTenant());

    // When
    filter.doFilterInternal(request, response, filterChain);

    // Then
    assertThat(response.getStatus()).isEqualTo(403);
    verify(filterChain, never()).doFilter(any(), any());
  }
}

