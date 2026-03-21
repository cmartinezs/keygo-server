package io.cmartinezs.keygo.supabase.clientapp.entity;

import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for ClientApp persistence.
 * <p>Entidad JPA para persistencia de ClientApp.
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
    name = "client_apps",
    indexes = {
      @Index(name = "idx_client_apps_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_client_apps_client_id", columnList = "client_id"),
      @Index(name = "idx_client_apps_status", columnList = "status")
    })
public class ClientAppEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private TenantEntity tenant;

  @Column(name = "client_id", nullable = false, unique = true, length = 255)
  private String clientId;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ClientType type;

  @Column(name = "hashed_secret")
  private String hashedSecret;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private ClientAppStatus status = ClientAppStatus.ACTIVE;

  @OneToMany(
      mappedBy = "clientApp",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<ClientRedirectUriEntity> redirectUris = new ArrayList<>();

  @OneToMany(
      mappedBy = "clientApp",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<ClientAllowedGrantEntity> allowedGrants = new ArrayList<>();

  @OneToMany(
      mappedBy = "clientApp",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<ClientAllowedScopeEntity> allowedScopes = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}

