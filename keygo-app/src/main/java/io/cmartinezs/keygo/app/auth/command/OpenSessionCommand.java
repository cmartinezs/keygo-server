package io.cmartinezs.keygo.app.auth.command;

import java.time.Instant;
import java.util.UUID;

/**
 * Comando: abrir una nueva sesión tras el canje exitoso de un authorization code o login de plataforma.
 *
 * <p>Modelo restructurado (RFC restructure-multitenant):
 * <ul>
 *   <li>{@code platformUserId} — UUID del platform_user (nullable para MVP — tenant-only users)
 *   <li>{@code clientAppId} — UUID de la app cliente (nullable — null = sesión de plataforma)
 * </ul>
 *
 * @param platformUserId UUID del platform_user (nullable para MVP)
 * @param clientAppId    UUID de la app cliente como String (nullable — null = sesión de plataforma)
 * @param expiresAt      momento de expiración de la sesión
 * @param now            instante actual (para auditabilidad)
 * @param userAgent      user-agent del cliente (puede ser null)
 * @param ipAddress      dirección IP del cliente (puede ser null)
 * @param signingKeyId   UUID de la clave RSA que firmó los tokens (puede ser null)
 */
public record OpenSessionCommand(
    UUID platformUserId,
    String clientAppId,
    Instant expiresAt,
    Instant now,
    String userAgent,
    String ipAddress,
    String signingKeyId) {}
