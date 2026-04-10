package io.cmartinezs.keygo.supabase.user.entity;

import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
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
 * JPA entity for tenant participation.
 * <p>Entidad JPA para pertenencia de un platform user a un tenant.
 * Local usernames remain tenant-scoped aliases, while credentials and profile live in
 * {@link PlatformUserEntity}.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "tenant_users",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_tenant_users_tenant_platform_user",
          columnNames = {"tenant_id", "platform_user_id"}),
      @UniqueConstraint(name = "uq_tenant_users_id_tenant", columnNames = {"id", "tenant_id"})
    },
    indexes = {
      @Index(name = "idx_tenant_users_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_tenant_users_platform_user", columnList = "platform_user_id"),
      @Index(name = "idx_tenant_users_status", columnList = "status")
    })
public class TenantUserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private TenantEntity tenant;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "platform_user_id", nullable = false)
  private PlatformUserEntity platformUser;

  @Column(name = "local_username", length = 100)
  private String localUsername;

  @Column(name = "display_name_override", length = 255)
  private String displayNameOverride;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private UserStatus status = UserStatus.ACTIVE;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Transient
  public String getUsername() {
    return localUsername;
  }

  public void setUsername(String username) {
    this.localUsername = username;
  }

  @Transient
  public String getEmail() {
    return platformUser != null ? platformUser.getEmail() : null;
  }

  public void setEmail(String email) {
    ensurePlatformUser().setEmail(email);
  }

  @Transient
  public String getPasswordHash() {
    return platformUser != null ? platformUser.getPasswordHash() : null;
  }

  public void setPasswordHash(String passwordHash) {
    ensurePlatformUser().setPasswordHash(passwordHash);
  }

  @Transient
  public String getFirstName() {
    return platformUser != null ? platformUser.getFirstName() : null;
  }

  public void setFirstName(String firstName) {
    ensurePlatformUser().setFirstName(firstName);
  }

  @Transient
  public String getLastName() {
    return platformUser != null ? platformUser.getLastName() : null;
  }

  public void setLastName(String lastName) {
    ensurePlatformUser().setLastName(lastName);
  }

  @Transient
  public String getPhoneNumber() {
    return platformUser != null ? platformUser.getPhoneNumber() : null;
  }

  public void setPhoneNumber(String phoneNumber) {
    ensurePlatformUser().setPhoneNumber(phoneNumber);
  }

  @Transient
  public String getLocale() {
    return platformUser != null ? platformUser.getLocale() : null;
  }

  public void setLocale(String locale) {
    ensurePlatformUser().setLocale(locale);
  }

  @Transient
  public String getZoneinfo() {
    return platformUser != null ? platformUser.getZoneinfo() : null;
  }

  public void setZoneinfo(String zoneinfo) {
    ensurePlatformUser().setZoneinfo(zoneinfo);
  }

  @Transient
  public String getProfilePictureUrl() {
    return platformUser != null ? platformUser.getProfilePictureUrl() : null;
  }

  public void setProfilePictureUrl(String profilePictureUrl) {
    ensurePlatformUser().setProfilePictureUrl(profilePictureUrl);
  }

  @Transient
  public LocalDate getBirthdate() {
    return platformUser != null ? platformUser.getBirthdate() : null;
  }

  public void setBirthdate(LocalDate birthdate) {
    ensurePlatformUser().setBirthdate(birthdate);
  }

  @Transient
  public String getWebsite() {
    return platformUser != null ? platformUser.getWebsite() : null;
  }

  public void setWebsite(String website) {
    ensurePlatformUser().setWebsite(website);
  }

  private PlatformUserEntity ensurePlatformUser() {
    if (platformUser == null) {
      platformUser = new PlatformUserEntity();
    }
    return platformUser;
  }
}
