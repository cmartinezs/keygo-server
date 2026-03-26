package io.cmartinezs.keygo.api.auth.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifica que {@link TokenRequest} deserializa correctamente JSON snake_case (estándar OAuth2)
 * a los campos camelCase de Java gracias a la estrategia global {@code SNAKE_CASE}.
 */
class TokenRequestJsonTest {

  private JsonMapper objectMapper;

  @BeforeEach
  void setUp() {
    // Replica la configuración global de ApplicationConfig (JsonMapperBuilderCustomizer)
    objectMapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .changeDefaultPropertyInclusion(i -> i.withValueInclusion(JsonInclude.Include.NON_NULL))
        .defaultTimeZone(TimeZone.getTimeZone("UTC"))
        .build();
  }

  @Test
  void givenSnakeCaseAuthorizationCodePayload_whenDeserialize_thenAllFieldsMapped() throws Exception {
    // Given — payload que envía keygo-ui en el paso 3 del flujo OAuth2
    String json = """
        {
          "grant_type": "authorization_code",
          "client_id": "keygo-ui",
          "code": "6u1tZL2i09axkWtNtA3pY7CUJ23sMyz5kD2P--Lw4TU",
          "code_verifier": "UGZxJfZqiyTqLo-JNU-Jgd-9JlJa1GwjGWb5vfG8xmdPX8KAohIy4ywAUU54A5JTH8OlYP4H0e2M81zfk3TMhg",
          "redirect_uri": "http://localhost:5173/callback"
        }
        """;

    // When
    TokenRequest request = objectMapper.readValue(json, TokenRequest.class);

    // Then
    assertThat(request.grantType()).isEqualTo("authorization_code");
    assertThat(request.clientId()).isEqualTo("keygo-ui");
    assertThat(request.code()).isEqualTo("6u1tZL2i09axkWtNtA3pY7CUJ23sMyz5kD2P--Lw4TU");
    assertThat(request.codeVerifier())
        .isEqualTo("UGZxJfZqiyTqLo-JNU-Jgd-9JlJa1GwjGWb5vfG8xmdPX8KAohIy4ywAUU54A5JTH8OlYP4H0e2M81zfk3TMhg");
    assertThat(request.redirectUri()).isEqualTo("http://localhost:5173/callback");
    assertThat(request.resolvedGrantType()).isEqualTo("authorization_code");
  }

  @Test
  void givenSnakeCaseRefreshTokenPayload_whenDeserialize_thenAllFieldsMapped() throws Exception {
    // Given
    String json = """
        {
          "grant_type": "refresh_token",
          "client_id": "keygo-ui",
          "refresh_token": "some-refresh-token-value"
        }
        """;

    // When
    TokenRequest request = objectMapper.readValue(json, TokenRequest.class);

    // Then
    assertThat(request.grantType()).isEqualTo("refresh_token");
    assertThat(request.clientId()).isEqualTo("keygo-ui");
    assertThat(request.refreshToken()).isEqualTo("some-refresh-token-value");
    assertThat(request.resolvedGrantType()).isEqualTo("refresh_token");
  }

  @Test
  void givenSnakeCaseClientCredentialsPayload_whenDeserialize_thenAllFieldsMapped() throws Exception {
    // Given
    String json = """
        {
          "grant_type": "client_credentials",
          "client_id": "m2m-app",
          "client_secret": "super-secret",
          "scope": "api:read"
        }
        """;

    // When
    TokenRequest request = objectMapper.readValue(json, TokenRequest.class);

    // Then
    assertThat(request.grantType()).isEqualTo("client_credentials");
    assertThat(request.clientId()).isEqualTo("m2m-app");
    assertThat(request.clientSecret()).isEqualTo("super-secret");
    assertThat(request.scope()).isEqualTo("api:read");
  }

  @Test
  void givenMissingGrantType_whenDeserialize_thenResolvedGrantTypeDefaultsToAuthorizationCode()
      throws Exception {
    // Given — grant_type omitido (comportamiento por defecto)
    String json = """
        {
          "client_id": "keygo-ui",
          "code": "some-code",
          "redirect_uri": "http://localhost:5173/callback"
        }
        """;

    // When
    TokenRequest request = objectMapper.readValue(json, TokenRequest.class);

    // Then
    assertThat(request.grantType()).isNull();
    assertThat(request.resolvedGrantType()).isEqualTo("authorization_code");
  }

  @Test
  void givenSnakeCaseClientIdWasNull_beforeFix_clientIdNowMapped() throws Exception {
    // Given — reproduce el bug original: client_id en snake_case no mapeaba a clientId
    String json = """
        {
          "grant_type": "authorization_code",
          "client_id": "keygo-ui",
          "code": "some-code",
          "code_verifier": "some-verifier",
          "redirect_uri": "http://localhost:5173/callback"
        }
        """;

    // When
    TokenRequest request = objectMapper.readValue(json, TokenRequest.class);

    // Then — antes del fix, clientId era null y @NotBlank lanzaba MethodArgumentNotValidException
    assertThat(request.clientId())
        .as("client_id debe mapearse a clientId (no debe ser null)")
        .isNotNull()
        .isEqualTo("keygo-ui");
  }
}

