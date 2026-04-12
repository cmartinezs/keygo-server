package io.cmartinezs.keygo.supabase.membership.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
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
@IdClass(AppMembershipRoleKey.class)
@Table(name = "app_membership_roles")
public class AppMembershipRoleEntity {

  @Id
  @Column(name = "membership_id", nullable = false)
  private UUID membershipId;

  @Id
  @Column(name = "role_id", nullable = false)
  private UUID roleId;

  @Column(name = "client_app_id", nullable = false)
  private UUID clientAppId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumns({
      @JoinColumn(name = "membership_id", referencedColumnName = "id", insertable = false, updatable = false),
      @JoinColumn(name = "client_app_id", referencedColumnName = "client_app_id", insertable = false, updatable = false)
  })
  private MembershipEntity membership;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumns({
      @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false),
      @JoinColumn(name = "client_app_id", referencedColumnName = "client_app_id", insertable = false, updatable = false)
  })
  private AppRoleEntity role;

  @CreationTimestamp
  @Column(name = "assigned_at", nullable = false, updatable = false)
  private OffsetDateTime assignedAt;
}
