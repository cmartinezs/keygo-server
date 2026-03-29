package io.cmartinezs.keygo.domain.billing.catalog.model;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Domain model for an app billing plan.
 * A plan is defined per ClientApp and offered to tenant subscribers.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class AppPlan {

  private final UUID id;
  private final UUID clientAppId;
  private final String code;
  private final String name;
  private final String description;
  private AppPlanStatus status;
  private final boolean isPublic;

  @Builder
  private AppPlan(
      UUID id,
      UUID clientAppId,
      String code,
      String name,
      String description,
      AppPlanStatus status,
      boolean isPublic) {
    if (clientAppId == null) throw new IllegalArgumentException("clientAppId cannot be null");
    if (code == null || code.isBlank()) throw new IllegalArgumentException("code cannot be blank");
    if (name == null || name.isBlank()) throw new IllegalArgumentException("name cannot be blank");
    if (status == null) throw new IllegalArgumentException("status cannot be null");

    this.id = id;
    this.clientAppId = clientAppId;
    this.code = code;
    this.name = name;
    this.description = description;
    this.status = status;
    this.isPublic = isPublic;
  }

  public boolean isActive() {
    return AppPlanStatus.ACTIVE.equals(this.status);
  }

  public void deactivate() {
    this.status = AppPlanStatus.INACTIVE;
  }
}
