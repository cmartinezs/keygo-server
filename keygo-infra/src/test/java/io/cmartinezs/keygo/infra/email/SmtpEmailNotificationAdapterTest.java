package io.cmartinezs.keygo.infra.email;

import io.cmartinezs.keygo.app.user.exception.EmailNotificationException;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SmtpEmailNotificationAdapter}.
 */
@ExtendWith(MockitoExtension.class)
class SmtpEmailNotificationAdapterTest {

  @Mock private JavaMailSender mailSender;

  @Mock private MimeMessage mimeMessage;

  private SmtpEmailNotificationAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new SmtpEmailNotificationAdapter(mailSender, "noreply@keygo.local", "KeyGo");
  }

  // ── buildVerificationBody ──────────────────────────────────────────────────

  @Test
  @DisplayName("buildVerificationBody — contiene el nombre de usuario")
  void buildVerificationBody_containsUsername() {
    // Given
    String username = "carlos";
    String code = "123456";

    // When
    String html = adapter.buildVerificationBody(username, code);

    // Then
    assertThat(html).contains("carlos");
  }

  @Test
  @DisplayName("buildVerificationBody — contiene el código de verificación")
  void buildVerificationBody_containsCode() {
    // Given / When
    String html = adapter.buildVerificationBody("usuario", "987654");

    // Then
    assertThat(html).contains("987654");
  }

  @Test
  @DisplayName("buildVerificationBody — contiene el nombre de la app")
  void buildVerificationBody_containsAppName() {
    // Given / When
    String html = adapter.buildVerificationBody("usuario", "000000");

    // Then
    assertThat(html).contains("KeyGo");
  }

  @Test
  @DisplayName("buildVerificationBody — es HTML válido (DOCTYPE + lang es)")
  void buildVerificationBody_isHtml() {
    // Given / When
    String html = adapter.buildVerificationBody("usuario", "000000");

    // Then
    assertThat(html).contains("<!DOCTYPE html>").contains("<html lang=\"es\">").contains("</html>");
  }

  @Test
  @DisplayName("buildVerificationBody — usa colores de paleta keygo-ui")
  void buildVerificationBody_usesKeyGoUiColors() {
    // Given / When
    String html = adapter.buildVerificationBody("usuario", "000000");

    // Then — indigo-600 (primario), indigo-700 (código), emerald-500 (aviso)
    assertThat(html).contains("#4f46e5").contains("#4338ca").contains("#10b981");
  }

  @Test
  @DisplayName("buildVerificationBody — texto está en español")
  void buildVerificationBody_isInSpanish() {
    // Given / When
    String html = adapter.buildVerificationBody("usuario", "000000");

    // Then
    assertThat(html)
        .contains("Verifica")
        .contains("código de verificación")
        .contains("válido por 30 minutos")
        .contains("¡Hola");
  }

  // ── sendVerificationEmail ──────────────────────────────────────────────────

  @Test
  @DisplayName("sendVerificationEmail — crea y envía MimeMessage")
  void sendVerificationEmail_createsMimeMessage() {
    // Given
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    // When
    adapter.sendVerificationEmail("user@example.com", "carlos", "654321");

    // Then
    verify(mailSender).createMimeMessage();
    verify(mailSender).send(mimeMessage);
  }

  @Test
  @DisplayName("sendVerificationEmail — lanza EmailNotificationException si MessagingException")
  void sendVerificationEmail_throwsOnMessagingException() throws MessagingException {
    // Given — hacer que setFrom() lance MessagingException para forzar el bloque catch
    MimeMessage badMessage = mock(MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(badMessage);
    doThrow(new MessagingException("SMTP error")).when(badMessage).setFrom(any(Address.class));

    // When / Then
    assertThatThrownBy(() -> adapter.sendVerificationEmail("fail@example.com", "usuario", "000000"))
        .isInstanceOf(EmailNotificationException.class)
        .hasMessageContaining("fail@example.com");
  }

  // ── sendTemporaryPasswordEmail ─────────────────────────────────────────────

  @Test
  @DisplayName("sendTemporaryPasswordEmail — crea y envía MimeMessage")
  void sendTemporaryPasswordEmail_createsMimeMessage() {
    // Given
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    // When
    adapter.sendTemporaryPasswordEmail("user@example.com", "carlos", "TmpPwd123!");

    // Then
    verify(mailSender).createMimeMessage();
    verify(mailSender).send(mimeMessage);
  }

  @Test
  @DisplayName("sendTemporaryPasswordEmail — lanza EmailNotificationException si MessagingException")
  void sendTemporaryPasswordEmail_throwsOnMessagingException() throws MessagingException {
    // Given
    MimeMessage badMessage = mock(MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(badMessage);
    doThrow(new MessagingException("SMTP error")).when(badMessage).setFrom(any(Address.class));

    // When / Then
    assertThatThrownBy(
            () -> adapter.sendTemporaryPasswordEmail("fail@example.com", "usuario", "TmpPwd123!"))
        .isInstanceOf(EmailNotificationException.class)
        .hasMessageContaining("fail@example.com");
  }

  // ── buildTemporaryPasswordBody ─────────────────────────────────────────────

  @Test
  @DisplayName("buildTemporaryPasswordBody — contiene el username y la contraseña")
  void buildTemporaryPasswordBody_containsCredentials() {
    // Given / When
    String html = adapter.buildTemporaryPasswordBody("carlos", "TmpPwd123!");

    // Then
    assertThat(html).contains("carlos").contains("TmpPwd123!");
  }

  @Test
  @DisplayName("buildTemporaryPasswordBody — es HTML válido en español")
  void buildTemporaryPasswordBody_isHtmlInSpanish() {
    // Given / When
    String html = adapter.buildTemporaryPasswordBody("usuario", "Pass!");

    // Then
    assertThat(html)
        .contains("<!DOCTYPE html>")
        .contains("<html lang=\"es\">")
        .contains("contraseña temporal")
        .contains("Bienvenido");
  }

  @Test
  @DisplayName("buildTemporaryPasswordBody — indica que es contraseña temporal")
  void buildTemporaryPasswordBody_indicatesTemporaryPassword() {
    // Given / When
    String html = adapter.buildTemporaryPasswordBody("usuario", "Pass!");

    // Then
    assertThat(html).contains("temporal");
  }

  // ── buildPasswordRecoveryBody ──────────────────────────────────────────────

  @Test
  @DisplayName("buildPasswordRecoveryBody — contiene el token de recuperación")
  void buildPasswordRecoveryBody_containsToken() {
    // Given / When
    String html = adapter.buildPasswordRecoveryBody("carlos", "abc123def456abc123def456abc12300", "acme");

    // Then
    assertThat(html).contains("abc123def456abc123def456abc12300");
  }

  @Test
  @DisplayName("buildPasswordRecoveryBody — es HTML válido en español")
  void buildPasswordRecoveryBody_isHtmlInSpanish() {
    // Given / When
    String html = adapter.buildPasswordRecoveryBody("carlos", "sometoken", "acme");

    // Then
    assertThat(html)
        .contains("<!DOCTYPE html>")
        .contains("<html lang=\"es\">")
        .contains("Recupera")
        .contains("token");
  }

  @Test
  @DisplayName("buildPasswordRecoveryBody — indica validez de 30 minutos")
  void buildPasswordRecoveryBody_indicates30MinTtl() {
    // Given / When
    String html = adapter.buildPasswordRecoveryBody("usuario", "sometoken", "acme");

    // Then
    assertThat(html).contains("30 minutos");
  }

  // ── sendPasswordRecoveryEmail ──────────────────────────────────────────────

  @Test
  @DisplayName("sendPasswordRecoveryEmail — crea y envía MimeMessage")
  void sendPasswordRecoveryEmail_createsMimeMessage() {
    // Given
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    // When
    adapter.sendPasswordRecoveryEmail("user@example.com", "carlos", "abc123token", "acme");

    // Then
    verify(mailSender).createMimeMessage();
    verify(mailSender).send(mimeMessage);
  }

  @Test
  @DisplayName("sendPasswordRecoveryEmail — lanza EmailNotificationException si MessagingException")
  void sendPasswordRecoveryEmail_throwsOnMessagingException() throws MessagingException {
    // Given
    MimeMessage badMessage = mock(MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(badMessage);
    doThrow(new MessagingException("SMTP error")).when(badMessage).setFrom(any(Address.class));

    // When / Then
    assertThatThrownBy(
            () -> adapter.sendPasswordRecoveryEmail("fail@example.com", "usuario", "abc123", "acme"))
        .isInstanceOf(EmailNotificationException.class)
        .hasMessageContaining("fail@example.com");
  }
}

