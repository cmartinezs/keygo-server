package io.cmartinezs.keygo.supabase.membership.entity;

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
 * JPA entity for platform-level role persistence.
 * <p>Entidad JPA para persistencia de roles de plataforma (Keygo).
 * Platform roles define authorization at the Keygo platform level,
 * independent of any specific tenant or application.
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
    name = "platform_roles",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_platform_roles_code", columnNames = {"code"})
    },
    indexes = {
      @Index(name = "idx_platform_roles_code", columnList = "code")
    })
public class PlatformRoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
