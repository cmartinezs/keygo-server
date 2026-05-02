package io.cmartinezs.keygo.domain.clientapp.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccessPolicyTest {

  @Test
  void constructor_validPolicy_shouldCreate() {
    // Given
    Set<AllowedGrant> grants = Set.of(AllowedGrant.AUTHORIZATION_CODE);
    Set<AllowedScope> scopes = Set.of(AllowedScope.of("openid"));

    // When
    AccessPolicy policy = new AccessPolicy(grants, scopes);

    // Then
    assertThat(policy.grants()).contains(AllowedGrant.AUTHORIZATION_CODE);
    assertThat(policy.scopes()).hasSize(1);
  }

  @Test
  void constructor_nullGrants_shouldThrow() {
    // Given
    Set<AllowedScope> scopes = Set.of();

    // When / Then
    assertThatThrownBy(() -> new AccessPolicy(null, scopes))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void constructor_emptyGrants_shouldThrow() {
    // Given
    Set<AllowedGrant> grants = Set.of();
    Set<AllowedScope> scopes = Set.of();

    // When / Then
    assertThatThrownBy(() -> new AccessPolicy(grants, scopes))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void constructor_nullScopes_shouldThrow() {
    // Given
    Set<AllowedGrant> grants = Set.of(AllowedGrant.CLIENT_CREDENTIALS);

    // When / Then
    assertThatThrownBy(() -> new AccessPolicy(grants, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void allowsGrant_presentGrant_shouldReturnTrue() {
    // Given
    AccessPolicy policy = new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of());

    // When / Then
    assertThat(policy.allowsGrant(AllowedGrant.AUTHORIZATION_CODE)).isTrue();
  }

  @Test
  void allowsGrant_absentGrant_shouldReturnFalse() {
    // Given
    AccessPolicy policy = new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of());

    // When / Then
    assertThat(policy.allowsGrant(AllowedGrant.CLIENT_CREDENTIALS)).isFalse();
  }

  @Test
  void allowsScope_presentScope_shouldReturnTrue() {
    // Given
    AccessPolicy policy =
        new AccessPolicy(
            Set.of(AllowedGrant.AUTHORIZATION_CODE),
            Set.of(AllowedScope.of("openid"), AllowedScope.of("profile")));

    // When / Then
    assertThat(policy.allowsScope(AllowedScope.of("openid"))).isTrue();
  }

  @Test
  void allowsScope_absentScope_shouldReturnFalse() {
    // Given
    AccessPolicy policy =
        new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of(AllowedScope.of("openid")));

    // When / Then
    assertThat(policy.allowsScope(AllowedScope.of("email"))).isFalse();
  }

  @Test
  void equals_sameContent_shouldReturnTrue() {
    // Given
    AccessPolicy first =
        new AccessPolicy(
            Set.of(AllowedGrant.AUTHORIZATION_CODE),
            Set.of(AllowedScope.of("openid"), AllowedScope.of("profile")));
    AccessPolicy second =
        new AccessPolicy(
            Set.of(AllowedGrant.AUTHORIZATION_CODE),
            Set.of(AllowedScope.of("profile"), AllowedScope.of("openid")));

    // When / Then
    assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
    assertThat(first.toString()).contains("AccessPolicy");
  }
}
