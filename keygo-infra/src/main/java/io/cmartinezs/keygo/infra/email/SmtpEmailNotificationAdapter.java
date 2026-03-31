package io.cmartinezs.keygo.infra.email;

import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * SMTP adapter for sending email notifications as HTML.
 * <p>Adaptador SMTP para el envío de notificaciones por email en formato HTML.
 * Uses Spring's JavaMailSender to send styled HTML emails.
 * Wired as a @Bean in ApplicationConfig — does NOT use @Component.
 * <p>Usa JavaMailSender de Spring para enviar emails HTML con estilos.
 * Instanciado como @Bean en ApplicationConfig — no usa @Component.
 *
 * @author cmartinezs
 * @version 2.0
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
  public void sendTemporaryPasswordEmail(String toEmail, String username, String rawPassword) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromAddress);
      helper.setTo(toEmail);
      helper.setSubject(appName + " — Tu cuenta ha sido creada");
      helper.setText(buildTemporaryPasswordBody(username, rawPassword), true);
      mailSender.send(message);
    } catch (MessagingException e) {
      throw new IllegalStateException("Error al enviar el email de contraseña temporal a " + toEmail, e);
    }
  }

  String buildTemporaryPasswordBody(String username, String rawPassword) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>Tu cuenta en %1$s</title>
        </head>
        <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:Inter,ui-sans-serif,system-ui,-apple-system,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f1f5f9;padding:40px 0;">
            <tr>
              <td align="center">
                <table width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%%;">

                  <!-- ── Header ── -->
                  <tr>
                    <td style="background:linear-gradient(135deg,#4f46e5 0%%,#6366f1 100%%);border-radius:12px 12px 0 0;padding:36px 40px;text-align:center;">
                      <div style="display:inline-flex;align-items:center;gap:10px;">
                        <div style="width:36px;height:36px;background:rgba(255,255,255,0.2);border-radius:8px;display:inline-flex;align-items:center;justify-content:center;">
                          <span style="font-size:20px;line-height:1;">🔑</span>
                        </div>
                        <span style="color:#ffffff;font-size:22px;font-weight:700;letter-spacing:-0.5px;">%1$s</span>
                      </div>
                      <p style="margin:12px 0 0;color:rgba(255,255,255,0.85);font-size:14px;">Tu cuenta ha sido creada</p>
                    </td>
                  </tr>

                  <!-- ── Body ── -->
                  <tr>
                    <td style="background:#ffffff;padding:40px 40px 32px;border-left:1px solid #e2e8f0;border-right:1px solid #e2e8f0;">

                      <p style="margin:0 0 8px;font-size:20px;font-weight:700;color:#0f172a;">¡Bienvenido/a, %2$s! 🎉</p>
                      <p style="margin:0 0 28px;font-size:15px;color:#475569;line-height:1.6;">
                        Tu cuenta en <strong style="color:#4f46e5;">%1$s</strong> ha sido creada exitosamente.
                        A continuación encontrarás tus credenciales de acceso temporales:
                      </p>

                      <!-- Credenciales -->
                      <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:28px;border:1px solid #e2e8f0;border-radius:10px;overflow:hidden;">
                        <tr>
                          <td style="background:#f8fafc;padding:14px 20px;border-bottom:1px solid #e2e8f0;">
                            <span style="font-size:12px;font-weight:600;color:#94a3b8;letter-spacing:0.5px;text-transform:uppercase;">Usuario</span>
                            <p style="margin:4px 0 0;font-size:16px;font-weight:600;color:#0f172a;font-family:'Courier New',Courier,monospace;">%2$s</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#ffffff;padding:14px 20px;">
                            <span style="font-size:12px;font-weight:600;color:#94a3b8;letter-spacing:0.5px;text-transform:uppercase;">Contraseña temporal</span>
                            <p style="margin:4px 0 0;font-size:22px;font-weight:800;letter-spacing:4px;color:#4338ca;font-family:'Courier New',Courier,monospace;">%3$s</p>
                          </td>
                        </tr>
                      </table>

                      <!-- Aviso de cambio obligatorio -->
                      <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
                        <tr>
                          <td style="background:#fef9ee;border-left:4px solid #f59e0b;border-radius:0 8px 8px 0;padding:14px 18px;">
                            <p style="margin:0;font-size:13px;color:#475569;line-height:1.5;">
                              ⚠️ <strong>Esta es una contraseña temporal.</strong>
                              Al ingresar por primera vez, el sistema te pedirá que la cambies por una contraseña propia y segura.
                            </p>
                          </td>
                        </tr>
                      </table>

                      <!-- Aviso de seguridad -->
                      <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:0;">
                        <tr>
                          <td style="background:#f0fdf4;border-left:4px solid #10b981;border-radius:0 8px 8px 0;padding:14px 18px;">
                            <p style="margin:0;font-size:13px;color:#475569;line-height:1.5;">
                              🔒 <strong>Por tu seguridad:</strong> nunca compartas esta contraseña con nadie.
                              Si no solicitaste esta cuenta, por favor contáctanos de inmediato.
                            </p>
                          </td>
                        </tr>
                      </table>

                    </td>
                  </tr>

                  <!-- ── Footer ── -->
                  <tr>
                    <td style="background:#f8fafc;border:1px solid #e2e8f0;border-top:none;border-radius:0 0 12px 12px;padding:24px 40px;text-align:center;">
                      <p style="margin:0 0 6px;font-size:13px;color:#94a3b8;">
                        Este correo fue enviado automáticamente por <strong style="color:#64748b;">%1$s</strong>.
                        Por favor, no respondas a este mensaje.
                      </p>
                      <p style="margin:0;font-size:12px;color:#cbd5e1;">
                        © 2026 %1$s · Todos los derechos reservados
                      </p>
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """.formatted(appName, username, rawPassword);
  }

  @Override
  public void sendVerificationEmail(String toEmail, String username, String verificationCode) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromAddress);
      helper.setTo(toEmail);
      helper.setSubject(appName + " — Verifica tu dirección de correo");
      helper.setText(buildVerificationBody(username, verificationCode), true);
      mailSender.send(message);
    } catch (MessagingException e) {
      throw new IllegalStateException("Error al enviar el email de verificación a " + toEmail, e);
    }
  }

  String buildVerificationBody(String username, String verificationCode) {
    return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>Verifica tu correo — %1$s</title>
        </head>
        <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:Inter,ui-sans-serif,system-ui,-apple-system,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f1f5f9;padding:40px 0;">
            <tr>
              <td align="center">
                <table width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%%;">

                  <!-- ── Header ── -->
                  <tr>
                    <td style="background:linear-gradient(135deg,#4f46e5 0%%,#6366f1 100%%);border-radius:12px 12px 0 0;padding:36px 40px;text-align:center;">
                      <div style="display:inline-flex;align-items:center;gap:10px;">
                        <div style="width:36px;height:36px;background:rgba(255,255,255,0.2);border-radius:8px;display:inline-flex;align-items:center;justify-content:center;">
                          <span style="font-size:20px;line-height:1;">🔑</span>
                        </div>
                        <span style="color:#ffffff;font-size:22px;font-weight:700;letter-spacing:-0.5px;">%1$s</span>
                      </div>
                      <p style="margin:12px 0 0;color:rgba(255,255,255,0.85);font-size:14px;">Verificación de correo electrónico</p>
                    </td>
                  </tr>

                  <!-- ── Body ── -->
                  <tr>
                    <td style="background:#ffffff;padding:40px 40px 32px;border-left:1px solid #e2e8f0;border-right:1px solid #e2e8f0;">

                      <p style="margin:0 0 8px;font-size:20px;font-weight:700;color:#0f172a;">¡Hola, %2$s! 👋</p>
                      <p style="margin:0 0 28px;font-size:15px;color:#475569;line-height:1.6;">
                        Gracias por registrarte en <strong style="color:#4f46e5;">%1$s</strong>. Para activar tu cuenta,
                        ingresa el siguiente código de verificación en la aplicación:
                      </p>

                      <!-- Código de verificación -->
                      <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
                        <tr>
                          <td align="center">
                            <div style="display:inline-block;background:#eef2ff;border:2px solid #6366f1;border-radius:12px;padding:20px 48px;">
                              <span style="font-size:38px;font-weight:800;letter-spacing:10px;color:#4338ca;font-family:'Courier New',Courier,monospace;">%3$s</span>
                            </div>
                          </td>
                        </tr>
                      </table>

                      <!-- Aviso de validez -->
                      <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
                        <tr>
                          <td style="background:#f8fafc;border-left:4px solid #10b981;border-radius:0 8px 8px 0;padding:14px 18px;">
                            <p style="margin:0;font-size:13px;color:#475569;line-height:1.5;">
                              ⏱ <strong>Este código es válido por 30 minutos.</strong>
                              Si no lo usas en ese tiempo, deberás solicitar uno nuevo.
                            </p>
                          </td>
                        </tr>
                      </table>

                      <p style="margin:0;font-size:13px;color:#94a3b8;line-height:1.5;">
                        Si tú no creaste esta cuenta, puedes ignorar este mensaje con toda seguridad.
                        Nadie podrá acceder a tu cuenta sin el código de verificación.
                      </p>
                    </td>
                  </tr>

                  <!-- ── Footer ── -->
                  <tr>
                    <td style="background:#f8fafc;border:1px solid #e2e8f0;border-top:none;border-radius:0 0 12px 12px;padding:24px 40px;text-align:center;">
                      <p style="margin:0 0 6px;font-size:13px;color:#94a3b8;">
                        Este correo fue enviado automáticamente por <strong style="color:#64748b;">%1$s</strong>.
                        Por favor, no respondas a este mensaje.
                      </p>
                      <p style="margin:0;font-size:12px;color:#cbd5e1;">
                        © 2026 %1$s · Todos los derechos reservados
                      </p>
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """.formatted(appName, username, verificationCode);
  }
}
