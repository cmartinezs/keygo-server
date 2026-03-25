package io.cmartinezs.keygo.domain.clientapp.model;

import io.cmartinezs.keygo.domain.clientapp.exception.InvalidRedirectUriException;
import io.cmartinezs.keygo.domain.clientapp.exception.UnsupportedGrantTypeException;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientAppTest {

  private static final String CLIENT_ID_VALUE = "abc123";
  private static final String CLIENT_NAME = "My App";
  private static final String REDIRECT_URI = "https://example.com/callback";

  private ClientApp buildConfidentialApp() {
    return ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(TenantId.generate())
        .clientId(ClientId.of(CLIENT_ID_VALUE))
        .name(CLIENT_NAME)
        .type(ClientType.CONFIDENTIAL)
        .hashedSecret("$2a$10$hashed")
        .redirectUris(Set.of(RedirectUri.of(REDIRECT_URI)))
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  private ClientApp buildPublicApp() {
    return ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(TenantId.generate())
        .clientId(ClientId.of(CLIENT_ID_VALUE))
        .name(CLIENT_NAME)
        .type(ClientType.PUBLIC)
        .redirectUris(Set.of(RedirectUri.of(REDIRECT_URI)))
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  @Test
  void build_confidentialApp_shouldSucceed() {
    // When
    ClientApp app = buildConfidentialApp();

    // Then
    assertThat(app).isNotNull();
    assertThat(app.getClientId().value()).isEqualTo(CLIENT_ID_VALUE);
    assertThat(app.getType()).isEqualTo(ClientType.CONFIDENTIAL);
    assertThat(app.isActive()).isTrue();
  }

  @Test
  void build_publicApp_shouldSucceed() {
    // When
    ClientApp app = buildPublicApp();

    // Then
    assertThat(app).isNotNull();
    assertThat(app.getType()).isEqualTo(ClientType.PUBLIC);
    assertThat(app.getHashedSecret()).isNull();
  }

  @Test
  void build_confidentialWithoutSecret_shouldThrow() {
    // Given
    AccessPolicy accessPolicy = new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of());
    var builder = ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(TenantId.generate())
        .clientId(ClientId.of(CLIENT_ID_VALUE))
        .name(CLIENT_NAME)
        .type(ClientType.CONFIDENTIAL)
        .accessPolicy(accessPolicy)
        .status(ClientAppStatus.ACTIVE);

    // When / Then
    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void suspend_activeApp_shouldChangStatusToSuspended() {
    // Given
    ClientApp app = buildConfidentialApp();

    // When
    app.suspend();

    // Then
    assertThat(app.isSuspended()).isTrue();
    assertThat(app.isActive()).isFalse();
  }

  @Test
  void suspend_alreadySuspendedApp_shouldThrow() {
    // Given
    ClientApp app = buildConfidentialApp();
    app.suspend();

    // When / Then
    assertThatThrownBy(app::suspend)
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void activate_suspendedApp_shouldChangeStatusToActive() {
    // Given
    ClientApp app = buildConfidentialApp();
    app.suspend();

    // When
    app.activate();

    // Then
    assertThat(app.isActive()).isTrue();
  }

  @Test
  void validateRedirectUri_registeredUri_shouldNotThrow() {
    // Given
    ClientApp app = buildConfidentialApp();

    // When / Then — no exception
    app.validateRedirectUri(REDIRECT_URI);
  }

  @Test
  void validateRedirectUri_unregisteredUri_shouldThrow() {
    // Given
    ClientApp app = buildConfidentialApp();

    // When / Then
    assertThatThrownBy(() -> app.validateRedirectUri("https://evil.com/callback"))
        .isInstanceOf(InvalidRedirectUriException.class);
  }

  @Test
  void validateGrant_allowedGrant_shouldNotThrow() {
    // Given
    ClientApp app = buildConfidentialApp();

    // When / Then — no exception
    app.validateGrant(AllowedGrant.AUTHORIZATION_CODE);
  }

  @Test
  void validateGrant_notAllowedGrant_shouldThrow() {
    // Given
    ClientApp app = buildConfidentialApp();

    // When / Then
    assertThatThrownBy(() -> app.validateGrant(AllowedGrant.CLIENT_CREDENTIALS))
        .isInstanceOf(UnsupportedGrantTypeException.class);
  }

  @Test
  void rotateSecret_publicApp_shouldThrow() {
    // Given
    ClientApp app = buildPublicApp();

    // When / Then
    assertThatThrownBy(() -> app.rotateSecret("newHashedSecret"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void rotateSecret_confidentialApp_shouldUpdateSecret() {
    // Given
    ClientApp app = buildConfidentialApp();

    // When
    app.rotateSecret("$2a$10$newHashed");

    // Then
    assertThat(app.getHashedSecret()).isEqualTo("$2a$10$newHashed");
  }

  @Test
  void build_nullId_shouldThrow() {
    // Given
    AccessPolicy accessPolicy = new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of());
    var builder = ClientApp.builder()
        .tenantId(TenantId.generate())
        .clientId(ClientId.of(CLIENT_ID_VALUE))
        .name(CLIENT_NAME)
        .type(ClientType.PUBLIC)
        .accessPolicy(accessPolicy)
        .status(ClientAppStatus.ACTIVE);

    // When / Then
    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateInfo_withValidValues_shouldUpdateFields() {
    // Given
    ClientApp app = buildConfidentialApp();

    // When
    app.updateInfo("Renamed App", "New description");

    // Then
    assertThat(app.getName()).isEqualTo("Renamed App");
    assertThat(app.getDescription()).isEqualTo("New description");
  }

  @Test
  void updateInfo_withBlankName_shouldThrow() {
    // Given
    ClientApp app = buildConfidentialApp();

    // When / Then
    assertThatThrownBy(() -> app.updateInfo("  ", "desc"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void updateRedirectUris_withNull_shouldSetEmptySet() {
    // Given
    ClientApp app = buildConfidentialApp();

    // When
    app.updateRedirectUris(null);

    // Then
    assertThat(app.getRedirectUris()).isEmpty();
  }

  @Test
  void updateRedirectUris_withValues_shouldReplaceUris() {
    // Given
    ClientApp app = buildConfidentialApp();
    Set<RedirectUri> newUris = Set.of(RedirectUri.of("https://example.com/new-callback"));

    // When
    app.updateRedirectUris(newUris);

    // Then
    assertThat(app.getRedirectUris()).containsExactly(RedirectUri.of("https://example.com/new-callback"));
  }

  @Test
  void updateAccessPolicy_withNull_shouldThrow() {
    // Given
    ClientApp app = buildConfidentialApp();

    // When / Then
    assertThatThrownBy(() -> app.updateAccessPolicy(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("AccessPolicy");
  }

  @Test
  void updateAccessPolicy_withValidPolicy_shouldUpdatePolicy() {
    // Given
    ClientApp app = buildConfidentialApp();
    AccessPolicy newPolicy =
        new AccessPolicy(
            Set.of(AllowedGrant.AUTHORIZATION_CODE, AllowedGrant.REFRESH_TOKEN),
            Set.of(AllowedScope.of("openid")));

    // When
    app.updateAccessPolicy(newPolicy);

    // Then
    assertThat(app.getAccessPolicy()).isEqualTo(newPolicy);
  }

  @Test
  void rotateSecret_withBlankSecret_shouldThrow() {
    // Given
    ClientApp app = buildConfidentialApp();

    // When / Then
    assertThatThrownBy(() -> app.rotateSecret("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null or blank");
  }
}
