package io.cmartinezs.keygo.supabase.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entidad JPA: Preferencias de notificación del usuario.
 *
 * <p>Mapea la tabla {@code platform_user_notification_preferences} de la base de datos.
 * Las preferencias son globales por account y se comparten entre tenants.
 */
@Entity
@Table(name = "platform_user_notification_preferences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationPreferencesEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  /** Relación con el platform user propietario de las preferencias. */
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "platform_user_id", nullable = false)
  private PlatformUserEntity platformUser;

  @Column(name = "security_alerts_email", nullable = false)
  private boolean securityAlertsEmail;

  @Column(name = "security_alerts_in_app", nullable = false)
  private boolean securityAlertsInApp;

  @Column(name = "billing_alerts_email", nullable = false)
  private boolean billingAlertsEmail;

  @Column(name = "product_updates_email", nullable = false)
  private boolean productUpdatesEmail;

  @Column(name = "weekly_digest", nullable = false)
  private boolean weeklyDigest;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
