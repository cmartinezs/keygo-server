package io.cmartinezs.keygo.supabase.membership.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AppMembershipRoleKey implements Serializable {
  private UUID membershipId;
  private UUID roleId;
}
