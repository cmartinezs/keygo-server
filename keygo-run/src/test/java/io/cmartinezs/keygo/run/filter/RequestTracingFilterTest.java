package io.cmartinezs.keygo.run.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RequestTracingFilter}.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>A {@code traceId} is always placed in the MDC and the response header.</li>
 *   <li>The {@code X-Request-ID} header value is reused as the trace ID when present.</li>
 *   <li>MDC is cleaned up after every request, including on exception.</li>
 *   <li>The filter chain is always called.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RequestTracingFilterTest {

  @Mock
  private FilterChain filterChain;

  private RequestTracingFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    filter   = new RequestTracingFilter();
    request  = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    MDC.clear();
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  // ─── traceId generation ───────────────────────────────────────────────────

  @Test
  void shouldGenerateTraceIdAndAddItToResponseHeader() throws ServletException, IOException {
    // Given
    request.setMethod("GET");
    request.setServletPath("/api/v1/tenants");

    // When
    filter.doFilter(request, response, filterChain);

    // Then
    String traceId = response.getHeader(RequestTracingFilter.TRACE_ID_HEADER);
    assertThat(traceId).isNotNull().isNotBlank();
  }

  @Test
  void shouldReuseClientXTraceIdHeaderAsTraceId() throws ServletException, IOException {
    // Given — the UI sends X-Trace-ID; the server echoes it back unchanged
    String clientTraceId = "client-provided-trace-id-12345";
    request.addHeader(RequestTracingFilter.TRACE_ID_HEADER, clientTraceId);
    request.setMethod("POST");
    request.setServletPath("/api/v1/tenants");

    // When
    filter.doFilter(request, response, filterChain);

    // Then
    assertThat(response.getHeader(RequestTracingFilter.TRACE_ID_HEADER))
        .isEqualTo(clientTraceId);
  }

  @Test
  void shouldIgnoreBlankXTraceIdAndGenerateNewTraceId() throws ServletException, IOException {
    // Given — blank header should not be reused
    request.addHeader(RequestTracingFilter.TRACE_ID_HEADER, "   ");
    request.setMethod("GET");
    request.setServletPath("/api/v1/service/info");

    // When
    filter.doFilter(request, response, filterChain);

    // Then
    String traceId = response.getHeader(RequestTracingFilter.TRACE_ID_HEADER);
    assertThat(traceId).isNotBlank().isNotEqualTo("   ");
  }

  // ─── MDC lifecycle ────────────────────────────────────────────────────────

  @Test
  void shouldClearMdcAfterSuccessfulRequest() throws ServletException, IOException {
    // Given
    request.setMethod("GET");
    request.setServletPath("/api/v1/tenants");

    // When
    filter.doFilter(request, response, filterChain);

    // Then — MDC must be clean after the filter completes
    assertThat(MDC.get(RequestTracingFilter.MDC_TRACE_ID)).isNull();
    assertThat(MDC.get(RequestTracingFilter.MDC_METHOD)).isNull();
    assertThat(MDC.get(RequestTracingFilter.MDC_PATH)).isNull();
  }

  @Test
  void shouldClearMdcEvenWhenFilterChainThrowsRuntimeException()
      throws ServletException, IOException {
    // Given
    request.setMethod("DELETE");
    request.setServletPath("/api/v1/tenants/acme");
    doThrow(new RuntimeException("unexpected error")).when(filterChain).doFilter(any(), any());

    // When + Then — exception propagates but MDC is still cleaned
    assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("unexpected error");

    assertThat(MDC.get(RequestTracingFilter.MDC_TRACE_ID)).isNull();
    assertThat(MDC.get(RequestTracingFilter.MDC_METHOD)).isNull();
    assertThat(MDC.get(RequestTracingFilter.MDC_PATH)).isNull();
  }

  @Test
  void shouldClearMdcEvenWhenFilterChainThrowsServletException()
      throws ServletException, IOException {
    // Given
    request.setMethod("GET");
    request.setServletPath("/api/v1/tenants");
    doThrow(new ServletException("servlet error")).when(filterChain).doFilter(any(), any());

    // When + Then
    assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
        .isInstanceOf(ServletException.class);

    assertThat(MDC.get(RequestTracingFilter.MDC_TRACE_ID)).isNull();
  }

  // ─── Filter chain ─────────────────────────────────────────────────────────

  @Test
  void shouldAlwaysCallFilterChain() throws ServletException, IOException {
    // Given
    request.setMethod("GET");
    request.setServletPath("/actuator/health");

    // When
    filter.doFilter(request, response, filterChain);

    // Then
    verify(filterChain, times(1)).doFilter(request, response);
  }

  // ─── MDC propagation to downstream chain ──────────────────────────────────

  @Test
  void shouldExposeTraceIdInMdcDuringFilterChainExecution()
      throws ServletException, IOException {
    // Given — same header used in both directions
    String clientTraceId = "propagation-test-id";
    request.addHeader(RequestTracingFilter.TRACE_ID_HEADER, clientTraceId);
    request.setMethod("POST");
    request.setServletPath("/api/v1/tenants/keygo/apps");

    // Capture MDC value during chain execution
    String[] capturedTraceId = new String[1];
    doAnswer(invocation -> {
      capturedTraceId[0] = MDC.get(RequestTracingFilter.MDC_TRACE_ID);
      return null;
    }).when(filterChain).doFilter(any(), any());

    // When
    filter.doFilter(request, response, filterChain);

    // Then — MDC was populated during chain, cleaned after
    assertThat(capturedTraceId[0]).isEqualTo(clientTraceId);
    assertThat(MDC.get(RequestTracingFilter.MDC_TRACE_ID)).isNull();
  }

  @Test
  void shouldExposeMethodAndPathInMdcDuringFilterChainExecution()
      throws ServletException, IOException {
    // Given
    request.setMethod("PATCH");
    request.setServletPath("/api/v1/tenants/keygo/account/profile");

    String[] capturedMethod = new String[1];
    String[] capturedPath   = new String[1];
    doAnswer(invocation -> {
      capturedMethod[0] = MDC.get(RequestTracingFilter.MDC_METHOD);
      capturedPath[0]   = MDC.get(RequestTracingFilter.MDC_PATH);
      return null;
    }).when(filterChain).doFilter(any(), any());

    // When
    filter.doFilter(request, response, filterChain);

    // Then
    assertThat(capturedMethod[0]).isEqualTo("PATCH");
    assertThat(capturedPath[0]).isEqualTo("/api/v1/tenants/keygo/account/profile");
  }
}
