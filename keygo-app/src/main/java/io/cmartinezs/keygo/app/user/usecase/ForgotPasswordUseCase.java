package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ForgotPasswordCommand;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.ForgotPasswordResult;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.shared.util.EmailMasker;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso: solicitar un token de recuperación de contraseña por email (self-service).
 *
 * <p>Flujo:
 * <ol>
 *   <li>Resolver el tenant por {@code tenantSlug}.</li>
 *   <li>Buscar el usuario por email — si no existe, retornar {@code sent=true} sin error
 *       (nunca revelar si el email existe para prevenir enumeración).</li>
 *   <li>Generar un token UUID de 32 chars hex con TTL de 30 minutos.</li>
 *   <li>Persistir el token via upsert (invalida token previo si existía).</li>
 *   <li>Enviar email con el token de recuperación.</li>
 * </ol>
 *
 * <p>Usado por: {@code POST /api/v1/tenants/{slug}/account/forgot-password}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ForgotPasswordUseCase {

  private static final int TOKEN_TTL_MINUTES = 30;

  private final TenantRepositoryPort tenantRepository;
  private final UserRepositoryPort userRepository;
  private final VerificationCodeRepositoryPort codeRepository;
  private final EmailNotificationPort emailNotification;

  public ForgotPasswordUseCase(
      TenantRepositoryPort tenantRepository,
      UserRepositoryPort userRepository,
      VerificationCodeRepositoryPort codeRepository,
      EmailNotificationPort emailNotification) {
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.codeRepository = codeRepository;
    this.emailNotification = emailNotification;
  }

  /**
   * Ejecuta la solicitud de recuperación de contraseña.
   *
   * @param command parámetros del comando
   * @return resultado con {@code sent = true} siempre (no revela si el email existe)
   * @throws TenantNotFoundException si el tenant no existe
   */
  public ForgotPasswordResult execute(ForgotPasswordCommand command) {
    // 1. Resolver tenant
    var tenant = tenantRepository.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    // 2. Buscar usuario por email — no revelar si no existe
    var userOpt = userRepository.findByTenantIdAndEmail(
        tenant.getId(), EmailAddress.of(command.email()));

    if (userOpt.isEmpty()) {
      // Anti-enumeration: retornar éxito silencioso con email de entrada ofuscado
      return new ForgotPasswordResult(true, EmailMasker.mask(command.email()));
    }

    var user = userOpt.get();

    // 3. Generar token hex de 32 chars (UUID sin guiones)
    String rawToken = UUID.randomUUID().toString().replace("-", "");
    Instant expiresAt = Instant.now().plus(TOKEN_TTL_MINUTES, ChronoUnit.MINUTES);

    // 4. Persistir (upsert — invalida token previo)
    var recoveryCode = VerificationCode.create(
        user.getId(), VerificationPurpose.PASSWORD_RECOVERY, rawToken, expiresAt);
    codeRepository.upsert(recoveryCode);

    // 5. Enviar email
    emailNotification.sendEmail(
        EmailNotificationPort.TYPE_PASSWORD_RECOVERY,
        command.email(),
        user.getUsername().value(),
        Map.of(
            "userUsername", user.getUsername().value(),
            "userFirstName", user.getFirstName() != null ? user.getFirstName() : "",
            "userLastName", user.getLastName() != null ? user.getLastName() : "",
            "recoveryToken", rawToken,
            "tenant_slug", command.tenantSlug()));

    return new ForgotPasswordResult(true, EmailMasker.mask(user.getEmail().value()));
  }
}
