package io.cmartinezs.keygo.api.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.cmartinezs.keygo.app.auth.result.OidcConfigurationResult;
import io.cmartinezs.keygo.app.auth.usecase.GetOidcConfigurationUseCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OidcMetadataControllerTest {

  @Mock GetOidcConfigurationUseCase getOidcConfigurationUseCase;

  @InjectMocks OidcMetadataController controller;

  MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  private OidcConfigurationResult buildResult(String slug) {
    String base = "http://localhost:8080/keygo-server/api/v1/tenants/" + slug;
    return new OidcConfigurationResult(
        base,
        base + "/oauth2/authorize",
        base + "/oauth2/token",
        base + "/.well-known/jwks.json",
        base + "/userinfo",
        List.of("code"),
        List.of("public"),
        List.of("RS256"),
        List.of("openid", "profile", "email"),
        List.of("none", "client_secret_basic"),
        List.of("authorization_code"),
        List.of("sub", "iss", "aud", "exp", "iat", "email"));
  }

  @Test
  void givenTenantSlug_whenGetOidcConfiguration_thenReturns200WithRequiredFields()
      throws Exception {
    // Given
    setUp();
    String slug = "my-tenant";
    when(getOidcConfigurationUseCase.execute(slug)).thenReturn(buildResult(slug));

    // When / Then
    mockMvc
        .perform(get("/api/v1/tenants/{slug}/.well-known/openid-configuration", slug))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.issuer").isString())
        .andExpect(jsonPath("$.authorization_endpoint").isString())
        .andExpect(jsonPath("$.token_endpoint").isString())
        .andExpect(
            jsonPath("$.jwks_uri").value(org.hamcrest.Matchers.endsWith("/.well-known/jwks.json")))
        .andExpect(jsonPath("$.response_types_supported").isArray())
        .andExpect(jsonPath("$.id_token_signing_alg_values_supported[0]").value("RS256"))
        .andExpect(jsonPath("$.grant_types_supported[0]").value("authorization_code"));
  }

  @Test
  void givenTenantSlug_whenGetOidcConfiguration_thenIssuerContainsSlug() throws Exception {
    // Given
    setUp();
    String slug = "acme-corp";
    when(getOidcConfigurationUseCase.execute(slug)).thenReturn(buildResult(slug));

    // When / Then
    mockMvc
        .perform(get("/api/v1/tenants/{slug}/.well-known/openid-configuration", slug))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.issuer").value(org.hamcrest.Matchers.containsString(slug)));
  }
}
