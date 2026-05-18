package io.cmartinezs.keygo.run;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.cmartinezs.keygo.run.aop.KeyGoTracingAspect;
import io.cmartinezs.keygo.run.aop.NotLog;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Unit tests for {@link KeyGoTracingAspect}.
 *
 * <p>The inner class {@link TracingTarget} lives in the {@code io.cmartinezs.keygo.run} package so
 * it matches the aspect pointcut {@code within(io.cmartinezs.keygo..*)} and is not excluded by
 * {@code !within(io.cmartinezs.keygo.run.aop..*)}.
 *
 * <p>Tests verify:
 * <ul>
 *   <li>Normal method calls produce {@code [TRACE_IN]} and {@code [TRACE_OUT]} log lines at DEBUG.</li>
 *   <li>Exceptions produce a {@code [TRACE_ERR]} log line and are re-thrown unchanged.</li>
 *   <li>Sensitive parameter names are masked with {@code [REDACTED]}.</li>
 *   <li>Sensitive fields inside complex objects are masked in the JSON output.</li>
 *   <li>Methods annotated with {@link NotLog} are skipped entirely.</li>
 *   <li>Getter/setter methods are excluded from tracing.</li>
 * </ul>
 */
@SpringJUnitConfig(classes = {
    KeyGoTracingAspect.class,
    KeyGoTracingAspectTest.TracingTarget.class
})
@EnableAspectJAutoProxy
@TestPropertySource(properties = "keygo.tracing.method-logging-enabled=true")
class KeyGoTracingAspectTest {


  @Autowired
  TracingTarget target;

  private ch.qos.logback.classic.Logger targetLogger;
  private ListAppender<ILoggingEvent> listAppender;

  @BeforeEach
  void setUp() {
    targetLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TracingTarget.class);
    targetLogger.setLevel(Level.DEBUG);
    listAppender = new ListAppender<>();
    listAppender.start();
    targetLogger.addAppender(listAppender);
  }

  @AfterEach
  void tearDown() {
    targetLogger.detachAppender(listAppender);
    listAppender.stop();
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  private List<String> capturedMessages() {
    return listAppender.list.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .toList();
  }

  // ─── Tests ───────────────────────────────────────────────────────────────

  @Test
  void shouldLogTraceInAndTraceOutForNormalMethod() {
    // Given / When
    String result = target.process("hello");

    // Then — simple String args are rendered without JSON quotes
    assertThat(result).isEqualTo("processed: hello");
    List<String> logs = capturedMessages();
    assertThat(logs)
        .anyMatch(m -> m.contains("[TRACE_IN]") && m.contains("process") && m.contains("input=hello"))
        .anyMatch(m -> m.contains("[TRACE_OUT]") && m.contains("process") && m.contains("processed: hello"))
        .noneMatch(m -> m.contains("TracingTarget.process"));
  }

  @Test
  void shouldLogTraceErrWhenMethodThrowsException() {
    // Given / When / Then
    assertThatThrownBy(() -> target.failing("bad"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid input");

    List<String> logs = capturedMessages();
    assertThat(logs)
        .anyMatch(m -> m.contains("[TRACE_IN]") && m.contains("failing"))
        .anyMatch(m ->
            m.contains("[TRACE_ERR]")
                && m.contains("failing")
                && m.contains("IllegalArgumentException")
                && m.contains("invalid input"))
        .noneMatch(m -> m.contains("TracingTarget.failing"));
  }

  @Test
  void shouldRedactSensitiveParameterNames() {
    // Given / When — parameter named "password" is redacted by name, never reaches formatValue()
    target.authenticate("user@example.com", "secret123");

    // Then
    List<String> logs = capturedMessages();
    assertThat(logs)
        .anyMatch(m -> m.contains("[TRACE_IN]") && m.contains("password=[REDACTED]"))
        .noneMatch(m -> m.contains("secret123"));
  }

  @Test
  void shouldRedactSensitiveFieldsInsideJsonOutput() {
    // Given — LoginRequest is a complex object serialized to JSON by Jackson
    // When
    target.login(new TracingTarget.LoginRequest("cmartinez", "B!f4Z2ab#@BB0E"));

    // Then — JSON output must have "password":"[REDACTED]", raw value must NOT appear
    List<String> logs = capturedMessages();
    assertThat(logs)
        .anyMatch(m ->
            m.contains("[TRACE_IN]")
                && m.contains("login")
                && m.contains("\"password\":\"[REDACTED]\""))
        .noneMatch(m -> m.contains("B!f4Z2ab#@BB0E"));
  }

  @Test
  void shouldRedactTokenFieldsInsideJsonResultOutput() {
    // Given / When — generateToken returns a complex object with an accessToken field
    target.generateToken("user1");

    // Then — con SNAKE_CASE el campo se serializa como "access_token"; debe quedar redactado
    List<String> logs = capturedMessages();
    assertThat(logs)
        .anyMatch(m ->
            m.contains("[TRACE_OUT]")
                && m.contains("generateToken")
                && m.contains("\"access_token\":\"[REDACTED]\""))
        .noneMatch(m -> m.contains("eyJsecretJWT"));
  }

  @Test
  void shouldRedactCompoundSensitiveFieldsInsideJsonOutput() {
    // Given — object with compound sensitive field names serialized to JSON
    target.withUserEntity(new TracingTarget.UserEntity("user@example.com", "$2b$12$hashed"));

    // Then — password_hash (snake_case of passwordHash) must be redacted
    List<String> logs = capturedMessages();
    assertThat(logs)
        .anyMatch(m ->
            m.contains("[TRACE_IN]")
                && m.contains("\"password_hash\":\"[REDACTED]\""))
        .noneMatch(m -> m.contains("$2b$12$hashed"));
  }

  @Test
  void shouldNotTraceMethodAnnotatedWithNoLog() {
    // Given / When
    String result = target.excludedMethod("data");

    // Then
    assertThat(result).isEqualTo("excluded: data");
    assertThat(capturedMessages()).noneMatch(m -> m.contains("excludedMethod"));
  }

  @Test
  void shouldNotTraceGetterMethods() {
    // Given / When
    String status = target.getStatus();

    // Then
    assertThat(status).isEqualTo("active");
    assertThat(capturedMessages()).noneMatch(m -> m.contains("getStatus"));
  }

  @Test
  void shouldHandleNullArgumentGracefully() {
    // Given / When
    String result = target.process(null);

    // Then
    assertThat(result).isEqualTo("processed: null");
    assertThat(capturedMessages())
        .anyMatch(m -> m.contains("[TRACE_IN]") && m.contains("input=null"));
  }

  // ─── Target class under test ─────────────────────────────────────────────

  /**
   * Sample Spring bean in package {@code io.cmartinezs.keygo.run} — matched by the
   * {@link KeyGoTracingAspect} pointcut.
   */
  @Component
  static class TracingTarget {

    /** Record with a sensitive field — Jackson serializes as {"emailOrUsername":"...","password":"..."} */
    record LoginRequest(String emailOrUsername, String password) {}

    /** Record with a compound sensitive field — Jackson + SNAKE_CASE serializes as {"email":"...","password_hash":"..."} */
    record UserEntity(String email, String passwordHash) {}

    /** Record with a sensitive result field — serialized as {"accessToken":"..."} */
    record TokenResult(String accessToken) {}

    public String process(String input) {
      return "processed: " + input;
    }

    public void failing(String reason) {
      throw new IllegalArgumentException("invalid input");
    }

    public void authenticate(String username, String password) {
      // intentionally empty
    }

    public void login(LoginRequest request) {
      // intentionally empty — request is serialized to JSON by the aspect
    }

    public void withUserEntity(UserEntity entity) {
      // intentionally empty — entity is serialized to JSON by the aspect
    }

    public TokenResult generateToken(String username) {
      return new TokenResult("eyJsecretJWT");
    }

    @NotLog
    public String excludedMethod(String data) {
      return "excluded: " + data;
    }

    public String getStatus() {
      return "active";
    }
  }
}

