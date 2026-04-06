package io.cmartinezs.keygo.api.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.cmartinezs.keygo.app.auth.usecase.GetJwksUseCase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class JwksControllerTest {

  @Mock GetJwksUseCase getJwksUseCase;

  @InjectMocks JwksController controller;

  MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void givenActiveKeys_whenGetJwks_thenReturns200WithKeysArray() throws Exception {
    // Given
    setUp();
    var jwkSet =
        Map.<String, Object>of(
            "keys", List.of(Map.of("kid", "kid-1", "kty", "RSA", "use", "sig", "alg", "RS256")));
    when(getJwksUseCase.execute("my-tenant")).thenReturn(jwkSet);

    // When / Then
    mockMvc
        .perform(get("/api/v1/tenants/my-tenant/.well-known/jwks.json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keys").isArray())
        .andExpect(jsonPath("$.keys[0].kid").value("kid-1"))
        .andExpect(jsonPath("$.keys[0].kty").value("RSA"));
  }

  @Test
  void givenNoKeys_whenGetJwks_thenReturns200WithEmptyArray() throws Exception {
    // Given
    setUp();
    var jwkSet = Map.<String, Object>of("keys", List.of());
    when(getJwksUseCase.execute("my-tenant")).thenReturn(jwkSet);

    // When / Then
    mockMvc
        .perform(get("/api/v1/tenants/my-tenant/.well-known/jwks.json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keys").isArray())
        .andExpect(jsonPath("$.keys").isEmpty());
  }
}
