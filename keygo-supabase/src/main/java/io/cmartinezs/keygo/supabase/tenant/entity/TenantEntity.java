package io.cmartinezs.keygo.supabase.tenant.entity;

import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for Tenant persistence.
 * <p>Entidad JPA para persistencia de Tenant.
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
    name = "tenants",
    indexes = {
      @Index(name = "idx_tenants_slug", columnList = "slug"),
      @Index(name = "idx_tenants_status", columnList = "status")
    })
public class TenantEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String slug;

  @Column(nullable = false)
  private String name;

  @Column(name = "owner_email", nullable = false)
  private String ownerEmail;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private TenantStatus status = TenantStatus.ACTIVE;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}

