package io.cmartinezs.keygo.app.auth.usecase;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class GetOidcConfigurationUseCaseTest {

  private final GetOidcConfigurationUseCase useCase =
      new GetOidcConfigurationUseCase("http://localhost:8080/keygo-server");

  @Test
  void givenTenantSlug_whenExecute_thenDocumentHasRequiredFields() {
    // Given
    String slug = "my-tenant";

    // When
    var result = useCase.execute(slug);

    // Then
    assertThat(result.issuer()).contains(slug);
    assertThat(result.jwksUri()).endsWith("/.well-known/jwks.json");
    assertThat(result.tokenEndpoint()).endsWith("/oauth2/token");
    assertThat(result.authorizationEndpoint()).endsWith("/oauth2/authorize");
    assertThat(result.responseTypesSupported()).contains("code");
    assertThat(result.idTokenSigningAlgSupported()).contains("RS256");
    assertThat(result.scopesSupported()).contains("openid");
    assertThat(result.grantTypesSupported()).contains("authorization_code");
  }

  @Test
  void givenTenantSlug_whenExecute_thenIssuerContainsTenantAndBaseUrl() {
    // Given / When
    var result = useCase.execute("acme-corp");

    // Then
    assertThat(result.issuer()).startsWith("http://localhost:8080/keygo-server");
    assertThat(result.issuer()).contains("acme-corp");
  }
}

