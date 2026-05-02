package io.cmartinezs.keygo.domain.membership.model;

import static org.junit.jupiter.api.Assertions.*;

import io.cmartinezs.keygo.domain.user.model.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlatformUserRoleTest {

  @Test
  void builder_withValidArgs_shouldCreate() {
    PlatformUserRole role = PlatformUserRole.builder()
        .id(new PlatformUserRoleId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000")))
        .userId(new UserId(UUID.fromString("660e8500-e29b-41d4-a716-446655440001")))
        .platformRoleId(new PlatformRoleId(UUID.fromString("770e8600-e29b-41d4-a716-446655440002")))
        .build();

    assertNotNull(role.getId());
    assertNotNull(role.getUserId());
    assertNotNull(role.getPlatformRoleId());
    assertEquals("GLOBAL", role.getScopeType());
  }

  @Test
  void builder_withNullId_shouldThrow() {
    assertThrows(IllegalArgumentException.class, () ->
        PlatformUserRole.builder()
            .userId(new UserId(UUID.randomUUID()))
            .platformRoleId(new PlatformRoleId(UUID.randomUUID()))
            .build()
    );
  }

  @Test
  void builder_withNullUserId_shouldThrow() {
    assertThrows(IllegalArgumentException.class, () ->
        PlatformUserRole.builder()
            .id(new PlatformUserRoleId(UUID.randomUUID()))
            .platformRoleId(new PlatformRoleId(UUID.randomUUID()))
            .build()
    );
  }

  @Test
  void builder_withNullPlatformRoleId_shouldThrow() {
    assertThrows(IllegalArgumentException.class, () ->
        PlatformUserRole.builder()
            .id(new PlatformUserRoleId(UUID.randomUUID()))
            .userId(new UserId(UUID.randomUUID()))
            .build()
    );
  }

  @Test
  void builder_withScopeType_shouldSet() {
    PlatformUserRole role = PlatformUserRole.builder()
        .id(new PlatformUserRoleId(UUID.randomUUID()))
        .userId(new UserId(UUID.randomUUID()))
        .platformRoleId(new PlatformRoleId(UUID.randomUUID()))
        .scopeType("CONTRACTOR")
        .build();

    assertEquals("CONTRACTOR", role.getScopeType());
  }
}