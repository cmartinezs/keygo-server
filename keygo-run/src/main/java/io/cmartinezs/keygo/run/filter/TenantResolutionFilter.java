package io.cmartinezs.keygo.run.filter;

import io.cmartinezs.keygo.app.tenant.context.TenantContextHolder;
import io.cmartinezs.keygo.app.tenant.usecase.GetTenantBySlugUseCase;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that resolves the current tenant from the {@code X-Tenant-Slug} HTTP header.
 *
 * <p>When the header is present:
 * <ol>
 *   <li>Looks up the tenant via {@link GetTenantBySlugUseCase}.</li>
 *   <li>If not found, sends 404 and stops the filter chain.</li>
 *   <li>If suspended, sends 403 and stops the filter chain.</li>
 *   <li>Otherwise, stores the slug in {@link TenantContextHolder} for downstream use.</li>
 * </ol>
 *
 * <p>When the header is absent, the filter is a no-op and the request proceeds without tenant context.
 * Future tenant-scoped endpoints must validate that {@link TenantContextHolder#isPresent()} returns true.
 *
 * <p>Filtro que resuelve el tenant actual desde el header HTTP {@code X-Tenant-Slug}.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantResolutionFilter extends OncePerRequestFilter {

  public static final String TENANT_SLUG_HEADER = "X-Tenant-Slug";

  private final GetTenantBySlugUseCase getTenantBySlugUseCase;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String tenantSlug = request.getHeader(TENANT_SLUG_HEADER);

    if (tenantSlug == null || tenantSlug.isBlank()) {
      // No tenant header — proceed without tenant context
      filterChain.doFilter(request, response);
      return;
    }

    try {
      Tenant tenant = getTenantBySlugUseCase.execute(tenantSlug);

      if (tenant.isSuspended()) {
        throw new TenantSuspendedException(tenantSlug);
      }

      TenantContextHolder.set(tenantSlug);
      log.debug("Tenant context established for slug: {}", tenantSlug);

      filterChain.doFilter(request, response);

    } catch (TenantNotFoundException ex) {
      log.warn("Tenant resolution failed — not found: {}", tenantSlug);
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          "Tenant not found: " + tenantSlug);

    } catch (TenantSuspendedException ex) {
      log.warn("Tenant resolution failed — suspended: {}", tenantSlug);
      response.sendError(HttpServletResponse.SC_FORBIDDEN,
          "Tenant is suspended: " + tenantSlug);

    } finally {
      TenantContextHolder.clear();
    }
  }
}

