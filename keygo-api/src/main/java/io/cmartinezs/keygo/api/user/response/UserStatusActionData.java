package io.cmartinezs.keygo.api.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.cmartinezs.keygo.app.user.result.UserStatusActionResult;

public record UserStatusActionData(
    @JsonProperty("user_id") String userId,
    @JsonProperty("previous_status") String previousStatus,
    @JsonProperty("current_status") String currentStatus,
    @JsonProperty("already_suspended") boolean alreadySuspended,
    @JsonProperty("already_active") boolean alreadyActive
) {

  public static UserStatusActionData fromSuspend(UserStatusActionResult result) {
    return new UserStatusActionData(
        result.userId().toString(),
        result.previousStatus().name(),
        result.currentStatus().name(),
        result.idempotent(),
        false);
  }

  public static UserStatusActionData fromActivate(UserStatusActionResult result) {
    return new UserStatusActionData(
        result.userId().toString(),
        result.previousStatus().name(),
        result.currentStatus().name(),
        false,
        result.idempotent());
  }
}
