package io.cmartinezs.keygo.supabase.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_event_payloads")
public class AuditEventPayloadEntity {

  @Id
  @Column(name = "audit_event_id", nullable = false)
  private UUID auditEventId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "audit_event_id", referencedColumnName = "id", insertable = false, updatable = false)
  private AuditEventEntity auditEvent;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "request_payload", columnDefinition = "jsonb")
  private String requestPayload;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "response_payload", columnDefinition = "jsonb")
  private String responsePayload;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "before_state", columnDefinition = "jsonb")
  private String beforeState;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "after_state", columnDefinition = "jsonb")
  private String afterState;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "diff_payload", columnDefinition = "jsonb")
  private String diffPayload;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "extra_context", nullable = false, columnDefinition = "jsonb")
  @Builder.Default
  private String extraContext = "{}";

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
