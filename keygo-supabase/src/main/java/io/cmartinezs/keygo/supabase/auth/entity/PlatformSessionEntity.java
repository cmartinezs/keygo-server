package io.cmartinezs.keygo.supabase.auth.entity;

import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
 * JPA entity for global platform sessions.
 */
@Entity
@Table(name = "platform_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSessionEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "platform_user_id", nullable = false)
  private PlatformUserEntity platformUser;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "last_accessed_at", nullable = false)
  private Instant lastAccessedAt;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;

  @Column(name = "login_method", length = 30)
  private String loginMethod;

  @Column(name = "auth_strength", length = 30)
  private String authStrength;

  @Column(name = "device_fingerprint", length = 255)
  private String deviceFingerprint;

  @Column(name = "browser_name", length = 100)
  private String browserName;

  @Column(name = "browser_version", length = 50)
  private String browserVersion;

  @Column(name = "os_name", length = 100)
  private String osName;

  @Column(name = "os_version", length = 50)
  private String osVersion;

  @Column(name = "device_type", length = 50)
  private String deviceType;

  @Column(name = "ip_address", length = 64)
  private String ipAddress;

  @Column(name = "forwarded_for", columnDefinition = "TEXT")
  private String forwardedFor;

  @Column(name = "country_code", length = 10)
  private String countryCode;

  @Column(name = "region", length = 100)
  private String region;

  @Column(name = "city", length = 100)
  private String city;

  @Column(name = "timezone_detected", length = 50)
  private String timezoneDetected;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt;

  @Column(name = "ended_at")
  private Instant endedAt;

  @Column(name = "termination_reason", length = 50)
  private String terminationReason;

  @Column(name = "last_route", length = 500)
  private String lastRoute;

  @Column(name = "last_activity_at")
  private Instant lastActivityAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
