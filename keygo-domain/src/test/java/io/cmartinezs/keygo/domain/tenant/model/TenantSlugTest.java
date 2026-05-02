package io.cmartinezs.keygo.domain.tenant.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TenantSlugTest {

  @Test
  void of_withValidSlug_shouldCreate() {
    TenantSlug slug = TenantSlug.of("my-tenant");

    assertEquals("my-tenant", slug.value());
  }

  @Test
  void of_withNull_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> TenantSlug.of(null));
  }

  @Test
  void of_withBlank_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> TenantSlug.of("   "));
  }

  @Test
  void of_withTooShort_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> TenantSlug.of("ab"));
  }

  @Test
  void of_withTooLong_shouldThrowException() {
    String tooLong = "a".repeat(101);
    assertThrows(IllegalArgumentException.class, () -> TenantSlug.of(tooLong));
  }

  @Test
  void of_withUppercase_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> TenantSlug.of("MyTenant"));
  }

  @Test
  void of_withSpecialChars_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> TenantSlug.of("my_tenant"));
  }

  @Test
  void of_startsWithHyphen_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> TenantSlug.of("-tenant"));
  }

  @Test
  void of_endsWithHyphen_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> TenantSlug.of("tenant-"));
  }

  @Test
  void fromName_withValidName_shouldCreate() {
    TenantSlug slug = TenantSlug.fromName("My Organization");

    assertEquals("my-organization", slug.value());
  }

  @Test
  void fromName_withNull_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> TenantSlug.fromName(null));
  }

  @Test
  void fromName_withEmpty_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () -> TenantSlug.fromName(""));
  }
}