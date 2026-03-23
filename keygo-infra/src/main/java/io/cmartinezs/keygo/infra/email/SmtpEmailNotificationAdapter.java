package io.cmartinezs.keygo.infra.email;

import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * SMTP adapter for sending email notifications.
 * <p>Adaptador SMTP para el envío de notificaciones por email.
 * Uses Spring's JavaMailSender to send plain-text emails.
 * Wired as a @Bean in ApplicationConfig — does NOT use @Component.
 * <p>Usa JavaMailSender de Spring para enviar emails de texto plano.
 * Instanciado como @Bean en ApplicationConfig — no usa @Component.
 * @author cmartinezs
 * @version 1.0
 */
public class SmtpEmailNotificationAdapter implements EmailNotificationPort {

  private final JavaMailSender mailSender;
  private final String fromAddress;
  private final String appName;

  public SmtpEmailNotificationAdapter(
      JavaMailSender mailSender,
      String fromAddress,
      String appName) {
    this.mailSender = mailSender;
    this.fromAddress = fromAddress;
    this.appName = appName;
  }

  @Override
  public void sendVerificationEmail(String toEmail, String username, String verificationCode) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(toEmail);
    message.setSubject(appName + " — Verification Code");
    message.setText(buildVerificationBody(username, verificationCode));
    mailSender.send(message);
  }

  private String buildVerificationBody(String username, String verificationCode) {
    return String.format("""
        Hello %s,

        Thank you for registering with %s.

        Your email verification code is:

            %s

        This code is valid for 30 minutes.
        If you did not register, please ignore this email.

        — The %s Team
        """,
        username,
        appName,
        verificationCode,
        appName);
  }
}

