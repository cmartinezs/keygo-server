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

/**
 * JPA entity for role hierarchy persistence.
 * <p>Represents a direct parent-child relationship between two AppRoles in the same client app.
 * Each role may appear at most once as a child (enforced by unique constraint on child_role_id).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "app_role_hierarchy",
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_role_hierarchy_child", columnNames = {"child_role_id"})
    },
    indexes = {
      @Index(name = "idx_role_hierarchy_child",  columnList = "child_role_id"),
      @Index(name = "idx_role_hierarchy_parent", columnList = "parent_role_id")
    })
public class AppRoleHierarchyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "child_role_id", nullable = false)
  private AppRoleEntity childRole;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "parent_role_id", nullable = false)
  private AppRoleEntity parentRole;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;
}
