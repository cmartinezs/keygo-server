package io.cmartinezs.keygo.supabase.auth.entity;

import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "signing_keys")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SigningKeyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "kid", nullable = false, unique = true, length = 255)
  private String kid;

  @Column(name = "algorithm", nullable = false, length = 20)
  private String algorithm;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "public_jwk", nullable = false, columnDefinition = "jsonb")
  private String publicMaterial;

  @Column(name = "private_key_encrypted", columnDefinition = "TEXT")
  private String privateMaterial;

  @Column(name = "activated_at")
  private Instant activatedAt;

  @Column(name = "retired_at")
  private Instant retiredAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id")
  private TenantEntity tenant;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
