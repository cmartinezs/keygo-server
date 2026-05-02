package io.cmartinezs.keygo.domain.membership.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PlatformRoleCodeTest {

  @Test
  void values_shouldExist() {
    assertEquals(3, PlatformRoleCode.values().length);
  }

  @Test
  void keygoAdmin_shouldHaveCorrectCode() {
    assertEquals("KEYGO_ADMIN", PlatformRoleCode.KEYGO_ADMIN.code());
    assertEquals("Keygo Admin", PlatformRoleCode.KEYGO_ADMIN.displayName());
  }

  @Test
  void keygoAccountAdmin_shouldHaveCorrectCode() {
    assertEquals("KEYGO_ACCOUNT_ADMIN", PlatformRoleCode.KEYGO_ACCOUNT_ADMIN.code());
  }

  @Test
  void keygoUser_shouldHaveCorrectCode() {
    assertEquals("KEYGO_USER", PlatformRoleCode.KEYGO_USER.code());
  }
}