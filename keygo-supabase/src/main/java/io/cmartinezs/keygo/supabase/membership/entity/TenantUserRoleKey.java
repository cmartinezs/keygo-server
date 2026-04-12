package io.cmartinezs.keygo.supabase.membership.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TenantUserRoleKey implements Serializable {
  private UUID tenantUserId;
  private UUID tenantRoleId;
}
