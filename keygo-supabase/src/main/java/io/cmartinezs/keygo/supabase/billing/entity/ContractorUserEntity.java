package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@IdClass(ContractorUserKey.class)
@Table(name = "contractor_users")
public class ContractorUserEntity {

  @Id
  @Column(name = "contractor_id", nullable = false)
  private UUID contractorId;

  @Id
  @Column(name = "platform_user_id", nullable = false)
  private UUID platformUserId;

  @Id
  @Column(name = "role_code", nullable = false, length = 50)
  private String roleCode;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "contractor_id", referencedColumnName = "id", insertable = false, updatable = false)
  private ContractorEntity contractor;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "platform_user_id", referencedColumnName = "id", insertable = false, updatable = false)
  private PlatformUserEntity platformUser;

  @CreationTimestamp
  @Column(name = "assigned_at", nullable = false, updatable = false)
  private OffsetDateTime assignedAt;
}
