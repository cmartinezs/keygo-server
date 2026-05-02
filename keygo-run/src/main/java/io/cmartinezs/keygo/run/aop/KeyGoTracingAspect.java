package io.cmartinezs.keygo.run.aop;

import java.lang.reflect.Parameter;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * AOP aspect that logs the input arguments and output result (or exception) of every public method
 * inside the {@code io.cmartinezs.keygo} package hierarchy.
 *
 * <h3>Activation</h3>
 *
 * <p>Enabled by default. Set {@code keygo.tracing.method-logging-enabled=false} in {@code
 * application.yml} to disable it entirely.
 *
 * <h3>Log level</h3>
 *
 * <p>All trace lines are emitted at {@code DEBUG}. In production profiles the root level for
 * {@code io.cmartinezs.keygo} is {@code INFO}, so nothing is printed unless you explicitly lower
 * the level with {@code logging.level.io.cmartinezs.keygo=DEBUG}.
 *
 * <h3>Exclusions</h3>
 *
 * <ul>
 *   <li>Getters, setters and boolean accessors (noise reduction)
 *   <li>Methods or classes annotated with {@link NotLog}
 *   <li>Classes inside {@code *.filter.*} packages (Servlet filters — CGLIB incompatibility)
 *   <li>This aspect itself (recursion guard)
 * </ul>
 *
 * <h3>Serialization</h3>
 *
 * <p>Simple types ({@code String}, {@code Number}, {@code Boolean}, {@code Enum}) are rendered
 * via {@code toString()}. Complex objects are serialized to JSON using the application's
 * {@link JsonMapper} bean (Jackson 3 / {@code tools.jackson.*}), which honours snake_case naming,
 * {@code NON_NULL} inclusion and all other global Jackson settings. If serialization fails for any
 * reason the value falls back to {@code toString()}.
 *
 * <h3>Sensitive data redaction</h3>
 *
 * <ul>
 *   <li><b>By parameter name</b> — if the parameter name contains a sensitive keyword (e.g.
 *       {@code password}, {@code token}) the whole argument is replaced with {@code [REDACTED]}.
 *   <li><b>Inside JSON output</b> — {@link #SENSITIVE_JSON_PATTERN} redacts {@code "field":"value"}
 *       pairs for known sensitive field names in the serialized JSON.
 *   <li><b>Inside toString() fallback</b> — {@link #SENSITIVE_TOSTRING_PATTERN} redacts
 *       {@code field=value} pairs in record/Lombok/manual toString() representations.
 * </ul>
 *
 * <h3>Log format</h3>
 *
 * <pre>
 *   [TRACE_IN]  methodName(param=value, obj={"field":"value"})
 *   [TRACE_OUT] methodName(param=value) → {"result":"value"} [42ms]
 *   [TRACE_ERR] methodName(param=value) ⚠ ExceptionType: message [5ms]
 * </pre>
 *
 * @author cmartinezs
 */
@Aspect
@Component
@ConditionalOnProperty(
    name = "keygo.tracing.method-logging-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class KeyGoTracingAspect {

  /** Maximum number of characters rendered per argument / return value. */
  private static final int MAX_VALUE_LENGTH = 300;

  /**
   * Parameter name substrings that cause the whole argument to be replaced with {@code [REDACTED]}.
   * Applied when the parameter name itself is sensitive (e.g. a raw password string).
   */
  private static final Set<String> SENSITIVE_PARAM_KEYWORDS =
      Set.of("password", "secret", "token", "credential", "api_key", "private_key", "hash", "pin");

  /**
   * Redacts sensitive field values in JSON output produced by Jackson.
   *
   * <p>Matches {@code "fieldName":"value"}, {@code "fieldName":null} and
   * {@code "fieldName":number} for all known sensitive field names (camelCase and snake_case).
   */
  private static final Pattern SENSITIVE_JSON_PATTERN = Pattern.compile(
      "(?i)\"(password|secret|token|credential|api[_\\-]?key|private[_\\-]?key|hash|pin"
          + "|access[_\\-]?token|refresh[_\\-]?token|bearer|jwt)\"\\s*:\\s*"
          + "(\"[^\"]*\"|null|\\d+)",
      Pattern.CASE_INSENSITIVE);

  /**
   * Redacts sensitive field values in {@code toString()} fallback output.
   *
   * <p>Matches {@code field=value} patterns found in Java record syntax
   * ({@code Record[field=value]}) and Lombok / manual toString ({@code Class{field=value}}).
   */
  private static final Pattern SENSITIVE_TOSTRING_PATTERN = Pattern.compile(
      "(?i)\\b(password|secret|token|credential|api[_\\-]?key|private[_\\-]?key|hash|pin"
          + "|access[_\\-]?token|refresh[_\\-]?token|bearer|jwt)=([^,\\]}\\s]*)",
      Pattern.CASE_INSENSITIVE);

  /**
   * Dedicated Jackson 3 mapper for tracing serialization.
   *
   * <p>Intentionally <b>not</b> a Spring-managed bean: injecting the application-wide
   * {@link JsonMapper} bean causes a circular dependency at startup because this aspect intercepts
   * factory methods inside {@code io.cmartinezs.keygo.*} — the same package where
   * {@code ApplicationConfig} and its {@code @Bean jsonMapperBuilderCustomizer} live. Spring
   * detects the cycle and refuses to start even with {@code @Autowired(required = false)}.
   *
   * <p>Configuration mirrors {@code ApplicationConfig#jsonMapperBuilderCustomizer()} exactly:
   * snake_case naming, NON_NULL inclusion, case-insensitive properties, UTC timezone,
   * and {@code FAIL_ON_UNKNOWN_PROPERTIES=false}.
   */
  private static final JsonMapper TRACER_MAPPER = JsonMapper.builder()
      .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
      .changeDefaultPropertyInclusion(
          include -> include.withValueInclusion(JsonInclude.Include.NON_NULL))
      .defaultTimeZone(TimeZone.getTimeZone("UTC"))
      .build();

  // ─── Pointcuts ─────────────────────────────────────────────────────────────

  /** Matches any type inside the KeyGo package hierarchy, excluding this aspect. */
  @Pointcut("within(io.cmartinezs.keygo..*) " + "&& !within(io.cmartinezs.keygo.run.aop..*)")
  public void keygoTypes() {
    // pointcut marker
  }

  /** Excludes trivial accessor methods to reduce log noise. */
  @Pointcut(
      "!execution(* *.get*(..)) " + "&& !execution(* *.set*(..)) " + "&& !execution(* *.is*(..))")
  public void notAccessor() {
    // pointcut marker
  }

  /**
   * Excludes classes inside any package named {@code filter}.
   *
   * <p>Servlet filters extend {@code GenericFilterBean} whose {@code logger} field is initialised
   * in the constructor. CGLIB/Objenesis bypasses the constructor when creating a proxy, leaving
   * {@code logger == null} and causing a {@link NullPointerException} at Tomcat startup.
   * Using {@code within()} (static) rather than {@code target(Filter)} (runtime) avoids the
   * issue of Spring AOP inadvertently suppressing advice on unrelated beans.
   */
  @Pointcut("!within(*..filter..*)")
  public void notFilter() {
    // pointcut marker
  }

  /** Combined pointcut: KeyGo types + not accessors + not Servlet filters. */
  @Pointcut("keygoTypes() && notAccessor() && notFilter()")
  public void keygoMethods() {
    // pointcut marker
  }

  // ─── Advice ────────────────────────────────────────────────────────────────

  /**
   * Around advice that traces every eligible method call.
   * Early-exits without any overhead when DEBUG is disabled for the target class logger.
   */
  @Around("keygoMethods() && !@annotation(io.cmartinezs.keygo.run.aop.NotLog) && !@within(io.cmartinezs.keygo.run.aop.NotLog)")
  public Object traceMethodExecution(ProceedingJoinPoint pjp) throws Throwable {
    MethodSignature signature = (MethodSignature) pjp.getSignature();

    // Use the declaring type (not the CGLIB proxy) so the logger name is clean
    Logger logger = LoggerFactory.getLogger(signature.getDeclaringType());

    // Fast-path: skip all reflection if DEBUG is off for this logger
    if (!logger.isDebugEnabled()) {
      return pjp.proceed();
    }

    String methodName = signature.getName();
    String argsStr = formatArgs(signature.getMethod().getParameters(), pjp.getArgs());

    logger.debug("[TRACE_IN]  {}({})", methodName, argsStr);

    long startMs = System.currentTimeMillis();
    try {
      Object result = pjp.proceed();
      long durationMs = System.currentTimeMillis() - startMs;
      logger.debug("[TRACE_OUT] {}({}) → {} [{}ms]",
          methodName, argsStr, formatValue(result), durationMs);
      return result;
    } catch (Throwable ex) {
      long durationMs = System.currentTimeMillis() - startMs;
      logger.debug("[TRACE_ERR] {}({}) ⚠ {}: {} [{}ms]",
          methodName, argsStr, ex.getClass().getSimpleName(), ex.getMessage(), durationMs);
      throw ex;
    }
  }

  // ─── Helpers ───────────────────────────────────────────────────────────────

  private String formatArgs(Parameter[] params, Object[] args) {
    if (args == null || args.length == 0) {
      return "";
    }
    return IntStream.range(0, Math.min(params.length, args.length))
        .mapToObj(i -> formatArg(params[i], args[i]))
        .collect(Collectors.joining(", "));
  }

  private String formatArg(Parameter param, Object arg) {
    String name = param.isNamePresent() ? param.getName() : "arg";
    boolean sensitive =
        SENSITIVE_PARAM_KEYWORDS.stream().anyMatch(k -> name.toLowerCase().contains(k));
    if (sensitive) {
      return name + "=[REDACTED]";
    }
    return name + "=" + formatValue(arg);
  }

  /**
   * Renders a value for the trace log.
   *
   * <ul>
   *   <li>Simple types ({@code String}, {@code Number}, {@code Boolean}, {@code Enum}) →
   *       {@code toString()} directly (no quotes, no JSON overhead).
   *   <li>Complex objects → JSON via {@link #TRACER_MAPPER} with sensitive-field redaction.
   *   <li>If Jackson serialization fails → {@code toString()} with regex redaction.
   * </ul>
   */
  private String formatValue(Object value) {
    if (value == null) {
      return "null";
    }

    // Simple/scalar types: render directly without Jackson overhead
    if (isSimpleType(value)) {
      String str = value.toString();
      return str.length() > MAX_VALUE_LENGTH ? str.substring(0, MAX_VALUE_LENGTH) + "..." : str;
    }

    // Complex objects: serialize to JSON and redact sensitive fields in the JSON output
    try {
      String json = TRACER_MAPPER.writeValueAsString(value);
      json = SENSITIVE_JSON_PATTERN.matcher(json)
          .replaceAll("\"$1\":\"[REDACTED]\"");
      return json.length() > MAX_VALUE_LENGTH ? json.substring(0, MAX_VALUE_LENGTH) + "..." : json;
    } catch (Exception ignored) {
      // fall through to toString() fallback
    }

    // Fallback: toString() with sensitive field redaction for record/Lombok/manual toString()
    String str = value.toString();
    str = SENSITIVE_TOSTRING_PATTERN.matcher(str).replaceAll("$1=[REDACTED]");
    return str.length() > MAX_VALUE_LENGTH ? str.substring(0, MAX_VALUE_LENGTH) + "..." : str;
  }

  private static boolean isSimpleType(Object value) {
    return value instanceof String
        || value instanceof Number
        || value instanceof Boolean
        || value instanceof Character
        || value.getClass().isEnum()
        || value.getClass().isPrimitive();
  }
}
