package io.cmartinezs.keygo.supabase.audit.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AuditEventTagKey implements Serializable {
  private UUID auditEventId;
  private String tag;
}
