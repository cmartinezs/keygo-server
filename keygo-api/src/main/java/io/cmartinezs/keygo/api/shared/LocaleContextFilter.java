package io.cmartinezs.keygo.api.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that resolves and propagates user locale for the duration of a request.
 *
 * <p>Uses LocaleContextHolder to store locale in request-scoped context, allowing
 * error handlers and other components to access it without parameter passing.
 *
 * <p>Must be registered with order HIGHEST_PRECEDENCE + 1 (after RequestTracingFilter)
 * to ensure tracing context is set up first.
 */
@Component
public class LocaleContextFilter extends OncePerRequestFilter {

  @Autowired
  private LocaleResolver localeResolver;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    // Resolve locale from Accept-Language header
    Locale locale = localeResolver.resolveLocale(request);

    // Set locale in LocaleContextHolder for request scope
    LocaleContextHolder.setLocale(locale);

    try {
      filterChain.doFilter(request, response);
    } finally {
      // Clean up LocaleContextHolder to prevent leaks in thread pools
      LocaleContextHolder.resetLocaleContext();
    }
  }
}
