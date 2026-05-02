package io.cmartinezs.keygo.supabase.membership.entity;

import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import jakarta.persistence.*;
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
 * JPA entity for app role persistence.
 * <p>Entidad JPA para persistencia de rol de app.
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
    name = "app_roles",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_app_roles_client_app_code", columnNames = {"client_app_id", "code"})
    },
    indexes = {
      @Index(name = "idx_app_roles_client_app_id", columnList = "client_app_id"),
      @Index(name = "idx_app_roles_code", columnList = "code")
    })
public class AppRoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "client_app_id", nullable = false, insertable = false, updatable = false)
  private UUID clientAppId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_app_id", nullable = false)
  private ClientAppEntity clientApp;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(name = "display_name", length = 255)
  private String displayName;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "is_default", nullable = false)
  private boolean isDefault;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
