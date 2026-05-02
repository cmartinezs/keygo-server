package io.cmartinezs.keygo.domain.user.model;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;

/**
 * Agregado: Preferencias de notificación del usuario.
 *
 * <p>Agrupa los 5 flags de preferencias que controlan qué notificaciones
 * recibe el usuario, tanto por email como in-app.
 *
 * <p>Si no hay registro persistido para el usuario, se retornan los valores
 * por defecto: alertas de seguridad y billing activadas, el resto desactivadas.
 *
 * <p>Sin dependencias Spring — pertenece a {@code keygo-domain}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class NotificationPreferences {

  private final UserId userId;
  private final TenantId tenantId;
  private boolean securityAlertsEmail;
  private boolean securityAlertsInApp;
  private boolean billingAlertsEmail;
  private boolean productUpdatesEmail;
  private boolean weeklyDigest;

  private NotificationPreferences(
      UserId userId,
      TenantId tenantId,
      boolean securityAlertsEmail,
      boolean securityAlertsInApp,
      boolean billingAlertsEmail,
      boolean productUpdatesEmail,
      boolean weeklyDigest) {
    this.userId = userId;
    this.tenantId = tenantId;
    this.securityAlertsEmail = securityAlertsEmail;
    this.securityAlertsInApp = securityAlertsInApp;
    this.billingAlertsEmail = billingAlertsEmail;
    this.productUpdatesEmail = productUpdatesEmail;
    this.weeklyDigest = weeklyDigest;
  }

  /**
   * Crea las preferencias por defecto para un usuario que nunca las configuró.
   *
   * <p>Valores por defecto:
   * <ul>
   *   <li>{@code securityAlertsEmail = true}</li>
   *   <li>{@code securityAlertsInApp = true}</li>
   *   <li>{@code billingAlertsEmail = true}</li>
   *   <li>{@code productUpdatesEmail = false}</li>
   *   <li>{@code weeklyDigest = false}</li>
   * </ul>
   *
   * @param userId   ID del usuario
   * @param tenantId ID del tenant
   * @return preferencias con valores por defecto
   */
  public static NotificationPreferences defaults(UserId userId, TenantId tenantId) {
    return new NotificationPreferences(userId, tenantId, true, true, true, false, false);
  }

  /**
   * Reconstituye las preferencias desde persistencia.
   *
   * @param userId               ID del usuario
   * @param tenantId             ID del tenant
   * @param securityAlertsEmail  alertas de seguridad por email
   * @param securityAlertsInApp  alertas de seguridad in-app
   * @param billingAlertsEmail   alertas de billing por email
   * @param productUpdatesEmail  actualizaciones de producto por email
   * @param weeklyDigest         resumen semanal por email
   * @return preferencias reconstituidas
   */
  public static NotificationPreferences reconstitute(
      UserId userId,
      TenantId tenantId,
      boolean securityAlertsEmail,
      boolean securityAlertsInApp,
      boolean billingAlertsEmail,
      boolean productUpdatesEmail,
      boolean weeklyDigest) {
    return new NotificationPreferences(
        userId, tenantId,
        securityAlertsEmail, securityAlertsInApp,
        billingAlertsEmail, productUpdatesEmail, weeklyDigest);
  }

  /**
   * Actualiza todos los valores de las preferencias.
   *
   * @param securityAlertsEmail  nuevo valor
   * @param securityAlertsInApp  nuevo valor
   * @param billingAlertsEmail   nuevo valor
   * @param productUpdatesEmail  nuevo valor
   * @param weeklyDigest         nuevo valor
   */
  public void update(
      boolean securityAlertsEmail,
      boolean securityAlertsInApp,
      boolean billingAlertsEmail,
      boolean productUpdatesEmail,
      boolean weeklyDigest) {
    this.securityAlertsEmail = securityAlertsEmail;
    this.securityAlertsInApp = securityAlertsInApp;
    this.billingAlertsEmail = billingAlertsEmail;
    this.productUpdatesEmail = productUpdatesEmail;
    this.weeklyDigest = weeklyDigest;
  }

  public UserId getUserId() { return userId; }
  public TenantId getTenantId() { return tenantId; }
  public boolean isSecurityAlertsEmail() { return securityAlertsEmail; }
  public boolean isSecurityAlertsInApp() { return securityAlertsInApp; }
  public boolean isBillingAlertsEmail() { return billingAlertsEmail; }
  public boolean isProductUpdatesEmail() { return productUpdatesEmail; }
  public boolean isWeeklyDigest() { return weeklyDigest; }
}
