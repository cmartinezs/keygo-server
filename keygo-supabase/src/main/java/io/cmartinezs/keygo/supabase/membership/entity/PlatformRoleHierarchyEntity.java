package io.cmartinezs.keygo.supabase.membership.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "platform_role_hierarchy")
public class PlatformRoleHierarchyEntity {

  @Id
  @Column(name = "child_role_id", nullable = false)
  private UUID childRoleId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "child_role_id", referencedColumnName = "id", insertable = false, updatable = false)
  private PlatformRoleEntity childRole;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "parent_role_id", nullable = false)
  private PlatformRoleEntity parentRole;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;
}
