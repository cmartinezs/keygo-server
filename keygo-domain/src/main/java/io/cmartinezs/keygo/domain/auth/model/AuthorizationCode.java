package io.cmartinezs.keygo.domain.auth.model;

import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.time.Instant;
import lombok.Getter;

/**
 * Agregado: Código de autorización OAuth 2.0.
 *
 * <p>Representa un código de autorización temporal que el usuario autoriza en la pantalla de
 * consentimiento y que la aplicación cliente puede canjear por un token de acceso.
 *
 * <p>Propiedades:
 * <ul>
 *   <li><b>id</b> — identificador único del código
 *   <li><b>code</b> — valor aleatorio del código (canje por token)
 *   <li><b>tenantId</b> — tenant propietario
 *   <li><b>clientAppId</b> — aplicación cliente que solicitó autorización
 *   <li><b>userId</b> — usuario que autorizó
 *   <li><b>redirectUri</b> — URI de redirección donde enviar el código
 *   <li><b>scopes</b> — permisos solicitados
 *   <li><b>codeChallenge</b> — desafío PKCE
 *   <li><b>status</b> — estado del código
 *   <li><b>expiresAt</b> — expiración (típicamente 10 minutos)
 *   <li><b>usedAt</b> — fecha de uso (si fue canjeado)
 * </ul>
 *
 * <p>Invariantes:
 * <ul>
 *   <li>Un código solo se puede usar una vez
 *   <li>Un código expira después de un tiempo breve
 *   <li>La redirección debe coincidir exactamente con la registrada en la app cliente
 * </ul>
 */
@Getter
public class AuthorizationCode {
  private final AuthorizationCodeId id;
  private final String code;
  private final TenantId tenantId;
  private final ClientAppId clientAppId;
  private final UserId userId;
  private final String redirectUri;
  private final ScopeSet scopes;
  private final CodeChallenge codeChallenge;
  private AuthorizationCodeStatus status;
  private final Instant expiresAt;
  private Instant usedAt;

  /**
   * Constructor privado. Usa el builder o el método factory.
   */
  private AuthorizationCode(
      AuthorizationCodeId id,
      String code,
      TenantId tenantId,
      ClientAppId clientAppId,
      UserId userId,
      String redirectUri,
      ScopeSet scopes,
      CodeChallenge codeChallenge,
      AuthorizationCodeStatus status,
      Instant expiresAt,
      Instant usedAt) {
    this.id = id;
    this.code = code;
    this.tenantId = tenantId;
    this.clientAppId = clientAppId;
    this.userId = userId;
    this.redirectUri = redirectUri;
    this.scopes = scopes;
    this.codeChallenge = codeChallenge;
    this.status = status;
    this.expiresAt = expiresAt;
    this.usedAt = usedAt;
  }

  /**
   * Factory method: crea un AuthorizationCode nuevo en estado PENDING.
   *
   * @param code valor aleatorio
   * @param tenantId tenant
   * @param clientAppId app cliente
   * @param userId usuario
   * @param redirectUri URI de redirección
   * @param scopes permisos
   * @param codeChallenge desafío PKCE
   * @param expiresAt fecha de expiración
   * @return AuthorizationCode nuevo
   */
  public static AuthorizationCode issued(
      String code,
      TenantId tenantId,
      ClientAppId clientAppId,
      UserId userId,
      String redirectUri,
      ScopeSet scopes,
      CodeChallenge codeChallenge,
      Instant expiresAt) {
    if (code == null || code.trim().isEmpty()) {
      throw new IllegalArgumentException("Code cannot be null or empty");
    }
    if (tenantId == null) {
      throw new IllegalArgumentException("TenantId cannot be null");
    }
    if (clientAppId == null) {
      throw new IllegalArgumentException("ClientAppId cannot be null");
    }
    if (userId == null) {
      throw new IllegalArgumentException("UserId cannot be null");
    }
    if (redirectUri == null || redirectUri.trim().isEmpty()) {
      throw new IllegalArgumentException("RedirectUri cannot be null or empty");
    }
    if (scopes == null) {
      throw new IllegalArgumentException("Scopes cannot be null");
    }
    if (codeChallenge == null) {
      throw new IllegalArgumentException("CodeChallenge cannot be null");
    }
    if (expiresAt == null) {
      throw new IllegalArgumentException("ExpiresAt cannot be null");
    }

    return new AuthorizationCode(
        AuthorizationCodeId.generate(),
        code.trim(),
        tenantId,
        clientAppId,
        userId,
        redirectUri.trim(),
        scopes,
        codeChallenge,
        AuthorizationCodeStatus.PENDING,
        expiresAt,
        null);
  }

  /**
   * Verifica que el código no ha expirado usando la hora actual del sistema.
   *
   * @return true si es válido (no expirado), false en caso contrario
   */
  public boolean isNotExpired() {
    return Instant.now().isBefore(expiresAt);
  }

  /**
   * Marca el código como usado. Solo aplica si está en estado PENDING.
   *
   * @param usedAt instante de uso
   * @throws IllegalStateException si el código ya fue usado o está revocado
   */
  public void markAsUsed(Instant usedAt) {
    if (status == AuthorizationCodeStatus.USED) {
      throw new IllegalStateException("Authorization code has already been used");
    }
    if (status == AuthorizationCodeStatus.REVOKED) {
      throw new IllegalStateException("Authorization code has been revoked");
    }
    if (status == AuthorizationCodeStatus.EXPIRED) {
      throw new IllegalStateException("Authorization code has expired");
    }

    this.status = AuthorizationCodeStatus.USED;
    this.usedAt = usedAt;
  }

  /**
   * Revoca el código. Solo aplica si está en estado PENDING.
   *
   * @throws IllegalStateException si el código ya fue usado o revocado
   */
  public void revoke() {
    if (status == AuthorizationCodeStatus.USED) {
      throw new IllegalStateException("Authorization code has already been used");
    }
    if (status == AuthorizationCodeStatus.REVOKED) {
      throw new IllegalStateException("Authorization code is already revoked");
    }

    this.status = AuthorizationCodeStatus.REVOKED;
  }

  /**
   * Marca el código como expirado.
   *
   * @throws IllegalStateException si el código ya fue usado o revocado
   */
  public void markAsExpired() {
    if (status == AuthorizationCodeStatus.USED) {
      throw new IllegalStateException("Authorization code has already been used");
    }
    if (status == AuthorizationCodeStatus.REVOKED) {
      throw new IllegalStateException("Authorization code is already revoked");
    }

    this.status = AuthorizationCodeStatus.EXPIRED;
  }


  @Override
  public String toString() {
    return "AuthorizationCode{"
        + "id="
        + id
        + ", tenantId="
        + tenantId
        + ", clientAppId="
        + clientAppId
        + ", status="
        + status
        + '}';
  }
}

