package io.cmartinezs.keygo.supabase.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA entity for global platform user identity persistence.
 *
 * <p>Represents a person's account at the KeyGo platform level, separate from tenant-scoped
 * participation ({@link TenantUserEntity}). Global credentials and profile fields live here.
 *
 * <p>RFC: docs/rfc/restructure-multitenant/02-modelo-identidad-multitenancy.md
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "platform_users",
    indexes = {
      @Index(name = "idx_platform_users_email", columnList = "email"),
      @Index(name = "idx_platform_users_status", columnList = "status"),
      @Index(name = "idx_platform_users_last_login_at", columnList = "last_login_at")
    })
public class PlatformUserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, columnDefinition = "citext")
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "first_name", length = 100)
  private String firstName;

  @Column(name = "last_name", length = 100)
  private String lastName;

  @Column(name = "display_name", length = 255)
  private String displayName;

  @Column(nullable = false, length = 30)
  @Builder.Default
  private String status = "ACTIVE";

  @Column(name = "email_verified_at")
  private OffsetDateTime emailVerifiedAt;

  @Column(name = "last_login_at")
  private OffsetDateTime lastLoginAt;

  @Column(name = "phone_number", length = 30)
  private String phoneNumber;

  @Column(name = "locale", length = 10)
  private String locale;

  @Column(name = "zoneinfo", length = 50)
  private String zoneinfo;

  @Column(name = "profile_picture_url", columnDefinition = "TEXT")
  private String profilePictureUrl;

  @Column(name = "birthdate")
  private LocalDate birthdate;

  @Column(name = "website", length = 2048)
  private String website;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
