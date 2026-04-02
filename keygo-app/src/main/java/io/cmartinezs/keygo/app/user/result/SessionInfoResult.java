package io.cmartinezs.keygo.app.user.result;

import java.time.Instant;
import java.util.UUID;

/**
 * Información de una sesión para la lista de sesiones del usuario autenticado.
 *
 * @param sessionId       UUID de la sesión
 * @param status          estado de la sesión (ACTIVE, TERMINATED, EXPIRED)
 * @param userAgent       User-Agent raw almacenado
 * @param ipAddress       dirección IP del cliente
 * @param createdAt       instante de creación de la sesión
 * @param lastAccessedAt  último acceso registrado
 * @param expiresAt       expiración de la sesión
 * @param isCurrent       true si coincide con la petición actual (mismos UA + IP)
 */
public record SessionInfoResult(
    UUID sessionId,
    String status,
    String userAgent,
    String ipAddress,
    Instant createdAt,
    Instant lastAccessedAt,
    Instant expiresAt,
    boolean isCurrent) {}
