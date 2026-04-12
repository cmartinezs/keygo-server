package io.cmartinezs.keygo.supabase.audit.entity;

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
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(AuditEventTagKey.class)
@Table(name = "audit_event_tags")
public class AuditEventTagEntity {

  @Id
  @Column(name = "audit_event_id", nullable = false)
  private UUID auditEventId;

  @Id
  @Column(name = "tag", nullable = false, length = 100)
  private String tag;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "audit_event_id", referencedColumnName = "id", insertable = false, updatable = false)
  private AuditEventEntity auditEvent;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
