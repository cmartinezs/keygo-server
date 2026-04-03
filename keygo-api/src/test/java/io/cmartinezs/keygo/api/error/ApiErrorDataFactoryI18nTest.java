package io.cmartinezs.keygo.api.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import java.lang.reflect.Field;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@ExtendWith(MockitoExtension.class)
class ApiErrorDataFactoryI18nTest {

  @Mock
  private MessageSource messageSource;

  @BeforeEach
  void setUp() {
    LocaleContextHolder.resetLocaleContext();
    new ApiErrorDataFactory(messageSource); // side-effect: registers itself as ApiErrorDataFactory.instance
  }

  @AfterEach
  void tearDown() throws Exception {
    // Reset the static instance to null to prevent test pollution
    Field instanceField = ApiErrorDataFactory.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
    LocaleContextHolder.resetLocaleContext();
  }

  // ── Español ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Resuelve mensaje en español para AUTHENTICATION_REQUIRED")
  void clientMessage_spanish_authenticationRequired() {
    // Given
    LocaleContextHolder.setLocale(Locale.of("es"));
    when(messageSource.getMessage("error.AUTHENTICATION_REQUIRED", null, Locale.of("es")))
        .thenReturn("No pudimos validar tu sesión. Inicia sesión nuevamente.");

    // When
    ErrorData errorData = ApiErrorDataFactory.fromException(
        ResponseCode.AUTHENTICATION_REQUIRED, null, false);

    // Then
    assertThat(errorData.getClientMessage())
        .isEqualTo("No pudimos validar tu sesión. Inicia sesión nuevamente.");
  }

  @Test
  @DisplayName("Resuelve mensaje en español (Chile) para INVALID_INPUT")
  void clientMessage_spanishChile_invalidInput() {
    // Given
    LocaleContextHolder.setLocale(Locale.of("es", "CL"));
    when(messageSource.getMessage("error.INVALID_INPUT", null, Locale.of("es", "CL")))
        .thenReturn("Revisa los datos que enviaste e intenta de nuevo.");

    // When
    ErrorData errorData = ApiErrorDataFactory.fromException(
        ResponseCode.INVALID_INPUT, null, false);

    // Then
    assertThat(errorData.getClientMessage())
        .isEqualTo("Revisa los datos que enviaste e intenta de nuevo.");
  }

  // ── Inglés ─────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Resuelve mensaje en inglés para RESOURCE_NOT_FOUND")
  void clientMessage_english_resourceNotFound() {
    // Given
    LocaleContextHolder.setLocale(Locale.US);
    when(messageSource.getMessage("error.RESOURCE_NOT_FOUND", null, Locale.US))
        .thenReturn("We couldn't find the resource you requested.");

    // When
    ErrorData errorData = ApiErrorDataFactory.fromException(
        ResponseCode.RESOURCE_NOT_FOUND, null, false);

    // Then
    assertThat(errorData.getClientMessage())
        .isEqualTo("We couldn't find the resource you requested.");
  }

  // ── Portugués ──────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Resuelve mensaje en portugués (Brasil) para DUPLICATE_RESOURCE")
  void clientMessage_portugueseBrazil_duplicateResource() {
    // Given
    LocaleContextHolder.setLocale(Locale.of("pt", "BR"));
    when(messageSource.getMessage("error.DUPLICATE_RESOURCE", null, Locale.of("pt", "BR")))
        .thenReturn("Este recurso já existe.");

    // When
    ErrorData errorData = ApiErrorDataFactory.fromException(
        ResponseCode.DUPLICATE_RESOURCE, null, false);

    // Then
    assertThat(errorData.getClientMessage())
        .isEqualTo("Este recurso já existe.");
  }

  // ── Francés ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Resuelve mensaje en francés para INSUFFICIENT_PERMISSIONS")
  void clientMessage_french_insufficientPermissions() {
    // Given
    LocaleContextHolder.setLocale(Locale.FRENCH);
    when(messageSource.getMessage("error.INSUFFICIENT_PERMISSIONS", null, Locale.FRENCH))
        .thenReturn("Vous n'avez pas la permission d'effectuer cette action.");

    // When
    ErrorData errorData = ApiErrorDataFactory.fromException(
        ResponseCode.INSUFFICIENT_PERMISSIONS, null, false);

    // Then
    assertThat(errorData.getClientMessage())
        .isEqualTo("Vous n'avez pas la permission d'effectuer cette action.");
  }

  // ── Fallback: locale no soportado → en-US default ──────────────────────────

  @Test
  @DisplayName("Fallback a en-US si MessageSource lanza excepción")
  void clientMessage_fallbackToEnUs_messageSourceException() {
    // Given — MessageSource throws exception, factory uses hardcoded fallback
    LocaleContextHolder.setLocale(Locale.of("es"));
    when(messageSource.getMessage("error.EMAIL_NOT_VERIFIED", null, Locale.of("es")))
        .thenThrow(new RuntimeException("Message not found")); // Simulates missing message

    // When
    ErrorData errorData = ApiErrorDataFactory.fromException(
        ResponseCode.EMAIL_NOT_VERIFIED, null, false);

    // Then — uses hardcoded fallback (en-US)
    assertThat(errorData.getClientMessage())
        .isEqualTo("You must verify your email before signing in.");
  }

  // ── ValidationErrors con i18n ──────────────────────────────────────────────

  @Test
  @DisplayName("fromValidationErrors resuelve mensaje localizado")
  void fromValidationErrors_i18n() {
    // Given
    LocaleContextHolder.setLocale(Locale.of("es"));
    when(messageSource.getMessage("error.INVALID_INPUT", null, Locale.of("es")))
        .thenReturn("Datos inválidos.");

    // When
    ErrorData errorData = ApiErrorDataFactory.fromValidationErrors(
        ResponseCode.INVALID_INPUT,
        java.util.List.of(),
        false);

    // Then
    assertThat(errorData.getClientMessage()).isEqualTo("Datos inválidos.");
  }

  @Test
  @DisplayName("fromValidationErrors establece origin y cause correctamente para errores de validación")
  void fromValidationErrors_setsOriginAndCauseForValidationErrors() {
    // Given
    LocaleContextHolder.setLocale(Locale.ENGLISH);
    when(messageSource.getMessage("error.INVALID_INPUT", null, Locale.ENGLISH))
        .thenReturn("Invalid input.");

    // When
    ErrorData errorData = ApiErrorDataFactory.fromValidationErrors(
        ResponseCode.INVALID_INPUT,
        java.util.List.of(),
        false);

    // Then
    assertThat(errorData.getOrigin()).isEqualTo(io.cmartinezs.keygo.api.error.ApiErrorOrigin.CLIENT_REQUEST);
    assertThat(errorData.getClientRequestCause()).isEqualTo(io.cmartinezs.keygo.api.error.ApiClientRequestCause.USER_INPUT);
  }

  @Test
  @DisplayName("fromValidationErrors incluye fieldErrors cuando hay errores de validación")
  void fromValidationErrors_includesFieldErrorsWhenPresent() {
    // Given
    LocaleContextHolder.setLocale(Locale.ENGLISH);
    when(messageSource.getMessage("error.INVALID_INPUT", null, Locale.ENGLISH))
        .thenReturn("Invalid input.");

    FieldValidationError fieldError1 = FieldValidationError.builder()
        .field("email")
        .message("must be a valid email address")
        .rejectedValue("invalid-email")
        .build();
    FieldValidationError fieldError2 = FieldValidationError.builder()
        .field("password")
        .message("must be at least 8 characters")
        .rejectedValue("short")
        .build();

    // When
    ErrorData errorData = ApiErrorDataFactory.fromValidationErrors(
        ResponseCode.INVALID_INPUT,
        java.util.List.of(fieldError1, fieldError2),
        false);

    // Then
    assertThat(errorData.getFieldErrors()).isNotNull().hasSize(2);
    assertThat(errorData.getFieldErrors()).extracting("field")
        .containsExactlyInAnyOrder("email", "password");
    assertThat(errorData.getFieldErrors()).extracting("message")
        .containsExactlyInAnyOrder("must be a valid email address", "must be at least 8 characters");
  }

  @Test
  @DisplayName("fromValidationErrors preserva los fieldErrors tal como se proporcionan")
  void fromValidationErrors_preservesFieldErrors() {
    // Given
    LocaleContextHolder.setLocale(Locale.ENGLISH);
    when(messageSource.getMessage("error.INVALID_INPUT", null, Locale.ENGLISH))
        .thenReturn("Invalid input.");

    FieldValidationError fieldError = FieldValidationError.builder()
        .field("username")
        .message("must not be blank")
        .rejectedValue("") // rejectedValue ya está filtrado en GlobalExceptionHandler
        .build();

    // When
    ErrorData errorData = ApiErrorDataFactory.fromValidationErrors(
        ResponseCode.INVALID_INPUT,
        java.util.List.of(fieldError),
        false); // production mode

    // Then
    assertThat(errorData.getFieldErrors()).isNotNull().hasSize(1);
    // ApiErrorDataFactory.fromValidationErrors() preserva los fieldErrors exactamente como se proporcionan.
    // El filtrado de rejectedValue se hace en GlobalExceptionHandler (línea 141 del filter)
    assertThat(errorData.getFieldErrors().getFirst().getField()).isEqualTo("username");
    assertThat(errorData.getFieldErrors().getFirst().getMessage()).isEqualTo("must not be blank");
  }

  @Test
  @DisplayName("fromValidationErrors establece fieldErrors=null cuando lista está vacía")
  void fromValidationErrors_nullifiesFieldErrorsWhenEmpty() {
    // Given
    LocaleContextHolder.setLocale(Locale.ENGLISH);
    when(messageSource.getMessage("error.INVALID_INPUT", null, Locale.ENGLISH))
        .thenReturn("Invalid input.");

    // When
    ErrorData errorData = ApiErrorDataFactory.fromValidationErrors(
        ResponseCode.INVALID_INPUT,
        java.util.List.of(),
        false);

    // Then
    assertThat(errorData.getFieldErrors()).isNull();
  }

  // ── Campos adicionales ─────────────────────────────────────────────────────

  @Test
  @DisplayName("ErrorData contiene traceId del MDC")
  void errorData_containsTraceId() {
    // Given
    org.slf4j.MDC.put("traceId", "abc123");
    LocaleContextHolder.setLocale(Locale.ENGLISH);
    when(messageSource.getMessage("error.BUSINESS_RULE_VIOLATION", null, Locale.ENGLISH))
        .thenReturn("This operation can't be completed.");

    // When
    ErrorData errorData = ApiErrorDataFactory.fromException(
        ResponseCode.BUSINESS_RULE_VIOLATION, null, false);

    // Then
    assertThat(errorData.getTraceId()).isEqualTo("abc123");

    // Cleanup
    org.slf4j.MDC.clear();
  }

  @Test
  @DisplayName("ErrorData establece code y origin correctamente")
  void errorData_hasCodeAndOrigin() {
    // Given
    LocaleContextHolder.setLocale(Locale.ENGLISH);
    when(messageSource.getMessage("error.INVALID_INPUT", null, Locale.ENGLISH))
        .thenReturn("Invalid input.");

    // When
    ErrorData errorData = ApiErrorDataFactory.fromException(
        ResponseCode.INVALID_INPUT, null, false);

    // Then
    assertThat(errorData.getCode()).isEqualTo("INVALID_INPUT");
    assertThat(errorData.getOrigin()).isEqualTo(ApiErrorOrigin.CLIENT_REQUEST);
  }
}
