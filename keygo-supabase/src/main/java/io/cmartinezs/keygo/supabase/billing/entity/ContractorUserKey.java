package io.cmartinezs.keygo.supabase.billing.entity;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContractorUserKey implements Serializable {
  private UUID contractorId;
  private UUID platformUserId;
  private String roleCode;
}
