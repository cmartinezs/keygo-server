package io.cmartinezs.keygo.run.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that assigns a unique trace ID to every incoming HTTP request and propagates it
 * through the SLF4J MDC so all log statements within the same request share the same {@code traceId}.
 *
 * <h3>Header contract (single, symmetric header)</h3>
 * <ul>
 *   <li><b>Request  →</b> client may send {@value TRACE_ID_HEADER} with its own UUID;
 *       if absent or blank, the server generates a new one.</li>
 *   <li><b>Response →</b> the server always echoes the effective trace ID in {@value TRACE_ID_HEADER}.</li>
 * </ul>
 *
 * <p>Using the same header name in both directions removes ambiguity: the UI interceptor only
 * needs to know one header name ({@value TRACE_ID_HEADER}).
 *
 * <h3>MDC keys managed by this filter</h3>
 * <ul>
 *   <li>{@value MDC_TRACE_ID} — trace ID (client-provided or server-generated UUID)</li>
 *   <li>{@value MDC_METHOD}   — HTTP method (GET, POST, …)</li>
 *   <li>{@value MDC_PATH}     — Servlet path (strips context-path, e.g. {@code /api/v1/tenants})</li>
 * </ul>
 *
 * <h3>Log lines emitted</h3>
 * <pre>
 *   [REQ_IN]  method=GET  path=/api/v1/tenants
 *   [REQ_OUT] method=GET  path=/api/v1/tenants  status=200  durationMs=42
 * </pre>
 *
 * <p>MDC is always cleared in {@code finally} — safe for thread-pool reuse.
 *
 * @author cmartinezs
 */
@Slf4j
public class RequestTracingFilter extends OncePerRequestFilter {

  /**
   * Single, symmetric trace header.
   * <ul>
   *   <li>UI → Server: send this header with a client-generated UUID to correlate frontend and backend logs.</li>
   *   <li>Server → UI: always present in the response — use it to display or log the trace ID on errors.</li>
   * </ul>
   */
  public static final String TRACE_ID_HEADER = "X-Trace-ID";

  /** MDC key for the trace identifier. */
  public static final String MDC_TRACE_ID = "traceId";

  /** MDC key for the HTTP method. */
  public static final String MDC_METHOD = "method";

  /** MDC key for the servlet path (no context-path prefix). */
  public static final String MDC_PATH = "path";

  @Override
  @SuppressWarnings("NullableProblems")
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String traceId = resolveTraceId(request);
    String method  = request.getMethod();
    String path    = request.getServletPath();

    MDC.put(MDC_TRACE_ID, traceId);
    MDC.put(MDC_METHOD, method);
    MDC.put(MDC_PATH, path);

    // Propagate trace ID back to the client immediately so it is present even on error responses
    response.setHeader(TRACE_ID_HEADER, traceId);

    long startMs = System.currentTimeMillis();
    log.debug("[REQ_IN] method={} path={}", method, path);

    try {
      filterChain.doFilter(request, response);
    } finally {
      long durationMs = System.currentTimeMillis() - startMs;
      int  status     = response.getStatus();
      log.debug("[REQ_OUT] method={} path={} status={} durationMs={}", method, path, status, durationMs);
      MDC.remove(MDC_TRACE_ID);
      MDC.remove(MDC_METHOD);
      MDC.remove(MDC_PATH);
    }
  }

  /**
   * Returns the trace ID to use for this request.
   * If the client provided an {@value TRACE_ID_HEADER} header, that value is reused;
   * otherwise a new random UUID is generated.
   */
  private String resolveTraceId(HttpServletRequest request) {
    String clientId = request.getHeader(TRACE_ID_HEADER);
    return (clientId != null && !clientId.isBlank()) ? clientId : UUID.randomUUID().toString();
  }
}

