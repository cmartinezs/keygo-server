package io.cmartinezs.keygo.run.filter;

import io.cmartinezs.keygo.api.shared.KeyGoLocaleResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that resolves the request locale from the {@code Accept-Language} header and
 * stores it in {@link LocaleContextHolder} for the duration of the request.
 *
 * <p><b>Why this filter is needed:</b>
 * Spring MVC's {@code DispatcherServlet} resolves the locale via the {@code localeResolver} bean
 * and stores it in {@code LocaleContextHolder}. However, requests that are rejected <em>before</em>
 * reaching the DispatcherServlet (e.g. 401 errors emitted directly by {@code BootstrapAdminKeyFilter})
 * never trigger that mechanism. Without this filter, {@code LocaleContextHolder.getLocale()} would
 * return the JVM default locale ({@code Locale.getDefault()}) for those early-rejected requests,
 * causing {@code ApiErrorDataFactory.clientMessage()} to ignore the client's language preference.
 *
 * <p><b>Execution order:</b>
 * This filter is registered at {@code Ordered.HIGHEST_PRECEDENCE + 1} (immediately after
 * {@code RequestTracingFilter}), so all subsequent filters and controllers benefit from the
 * pre-resolved locale.
 *
 * <p><b>Interaction with DispatcherServlet:</b>
 * For requests handled by the DispatcherServlet, the servlet overrides {@code LocaleContextHolder}
 * with the result of {@code localeResolver.resolveLocale(request)}. Because both this filter and
 * {@code I18nConfig.localeResolver()} use the same {@link KeyGoLocaleResolver} instance, the locale
 * stored at each stage is always consistent.
 *
 * @author cmartinezs
 * @version 1.0
 * @see KeyGoLocaleResolver
 * @see LocaleContextHolder
 */
@RequiredArgsConstructor
public class LocaleContextFilter extends OncePerRequestFilter {

  /** Shared resolver — same instance used by Spring MVC's DispatcherServlet. */
  private final KeyGoLocaleResolver localeResolver;

  /**
   * Resolves the locale for the current request and binds it to {@link LocaleContextHolder}.
   *
   * <p>The locale context is always cleared in the {@code finally} block to prevent
   * thread-local leaks in thread-pool environments.
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Locale locale = localeResolver.resolveLocale(request);
    // inheritable = true → locale propagates to @Async / CompletableFuture child threads
    LocaleContextHolder.setLocale(locale, true);
    try {
      filterChain.doFilter(request, response);
    } finally {
      LocaleContextHolder.resetLocaleContext();
    }
  }
}

