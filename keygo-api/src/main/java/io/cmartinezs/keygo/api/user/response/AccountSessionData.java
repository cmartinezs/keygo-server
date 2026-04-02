package io.cmartinezs.keygo.api.user.response;

import io.cmartinezs.keygo.api.user.util.UserAgentParser;
import io.cmartinezs.keygo.app.user.result.SessionInfoResult;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de presentación: información de una sesión para el endpoint de lista de sesiones.
 *
 * @param sessionId      UUID de la sesión
 * @param status         estado (ACTIVE, TERMINATED, EXPIRED)
 * @param browser        navegador inferido del User-Agent
 * @param os             sistema operativo inferido del User-Agent
 * @param deviceType     tipo de dispositivo (desktop, mobile, tablet)
 * @param ipAddress      IP del cliente
 * @param createdAt      instante de creación
 * @param lastAccessedAt último acceso registrado
 * @param expiresAt      expiración de la sesión
 * @param isCurrent      true si es la sesión de la petición actual
 */
public record AccountSessionData(
    UUID sessionId,
    String status,
    String browser,
    String os,
    String deviceType,
    String ipAddress,
    Instant createdAt,
    Instant lastAccessedAt,
    Instant expiresAt,
    boolean isCurrent) {

  /**
   * Convierte un {@link SessionInfoResult} en su representación de presentación.
   *
   * @param result resultado del caso de uso
   * @return DTO con los campos procesados para la UI
   */
  public static AccountSessionData from(SessionInfoResult result) {
    return new AccountSessionData(
        result.sessionId(),
        result.status(),
        UserAgentParser.extractBrowser(result.userAgent()),
        UserAgentParser.extractOs(result.userAgent()),
        UserAgentParser.extractDeviceType(result.userAgent()),
        result.ipAddress(),
        result.createdAt(),
        result.lastAccessedAt(),
        result.expiresAt(),
        result.isCurrent());
  }
}
