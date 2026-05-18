package io.cmartinezs.keygo.app.user.result;

import io.cmartinezs.keygo.domain.user.model.UserStatus;
import java.util.UUID;

public record UserStatusActionResult(
    UUID userId,
    UserStatus previousStatus,
    UserStatus currentStatus,
    boolean idempotent
) {}
