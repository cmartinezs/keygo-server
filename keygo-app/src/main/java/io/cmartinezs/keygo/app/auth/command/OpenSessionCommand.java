package io.cmartinezs.keygo.app.auth.command;

import java.time.Instant;

/**
 * Comando: abrir una nueva sesión de usuario tras el canje exitoso de un authorization code.
 *
 * @param tenantId    UUID del tenant como String
 * @param clientAppId UUID de la app cliente como String
 * @param userId      UUID del usuario como String
 * @param expiresAt   momento de expiración de la sesión
 * @param now         instante actual (para auditabilidad)
 * @param userAgent   user-agent del cliente (puede ser null)
 * @param ipAddress   dirección IP del cliente (puede ser null)
 */
public record OpenSessionCommand(
    String tenantId,
    String clientAppId,
    String userId,
    Instant expiresAt,
    Instant now,
    String userAgent,
    String ipAddress) {}

