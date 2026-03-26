package io.cmartinezs.keygo.api.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cmartinezs.keygo.api.error.GlobalExceptionHandler;
import io.cmartinezs.keygo.app.auth.result.AuthorizationInitiatedResult;
import io.cmartinezs.keygo.app.auth.usecase.AuthenticateUserForAuthorizationUseCase;
import io.cmartinezs.keygo.app.auth.usecase.ExchangeAuthorizationCodeUseCase;
import io.cmartinezs.keygo.app.auth.usecase.InitiateAuthorizationUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueAuthorizationCodeUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueClientCredentialsTokenUseCase;
import io.cmartinezs.keygo.app.auth.usecase.IssueTokensUseCase;
import io.cmartinezs.keygo.app.auth.usecase.OpenSessionUseCase;
import io.cmartinezs.keygo.app.auth.usecase.RotateRefreshTokenUseCase;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class AuthorizationControllerTest {

  @Mock private InitiateAuthorizationUseCase initiateAuthorizationUseCase;
  @Mock private AuthenticateUserForAuthorizationUseCase authenticateUserForAuthorizationUseCase;
  @Mock private IssueAuthorizationCodeUseCase issueAuthorizationCodeUseCase;
  @Mock private ExchangeAuthorizationCodeUseCase exchangeAuthorizationCodeUseCase;
  @Mock private IssueTokensUseCase issueTokensUseCase;
  @Mock private IssueClientCredentialsTokenUseCase issueClientCredentialsTokenUseCase;
  @Mock private OpenSessionUseCase openSessionUseCase;
  @Mock private RotateRefreshTokenUseCase rotateRefreshTokenUseCase;
  @Mock private RefreshTokenRepositoryPort refreshTokenRepository;
  @Mock private UserRepositoryPort userRepository;
  @Mock private TenantRepositoryPort tenantRepository;
  @Mock private ClientAppRepositoryPort clientAppRepository;
  @Mock private MembershipRepositoryPort membershipRepository;
  @Mock private ClockPort clock;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    var controller =
        new AuthorizationController(
            initiateAuthorizationUseCase,
            authenticateUserForAuthorizationUseCase,
            issueAuthorizationCodeUseCase,
            exchangeAuthorizationCodeUseCase,
            issueTokensUseCase,
            issueClientCredentialsTokenUseCase,
            openSessionUseCase,
            rotateRefreshTokenUseCase,
            refreshTokenRepository,
            userRepository,
            tenantRepository,
            clientAppRepository,
            membershipRepository,
            clock,
            "http://localhost:8080/keygo-server");

    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler(new MockEnvironment()))
            .build();
  }

  @Test
  void givenSnakeCaseOAuthQueryParams_whenAuthorize_thenReturns200() throws Exception {
    // Given
    when(initiateAuthorizationUseCase.execute(any()))
        .thenReturn(
            new AuthorizationInitiatedResult(
                "keygo-ui", "KeyGo UI", "http://localhost:5173/callback"));

    // When / Then
    mockMvc
        .perform(
            get("/api/v1/tenants/{tenantSlug}/oauth2/authorize", "keygo")
                .param("client_id", "keygo-ui")
                .param("redirect_uri", "http://localhost:5173/callback")
                .param("scope", "openid profile")
                .param("response_type", "code")
                .param("code_challenge", "abc")
                .param("code_challenge_method", "S256")
                .param("state", "state-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success.code").value("AUTHORIZATION_INITIATED"))
        .andExpect(request().sessionAttribute("authorizationState", org.hamcrest.Matchers.notNullValue()));

    verify(initiateAuthorizationUseCase).execute(any());
  }

  @Test
  void givenUnsupportedResponseType_whenAuthorize_thenReturns400() throws Exception {
    // Given / When / Then
    mockMvc
        .perform(
            get("/api/v1/tenants/{tenantSlug}/oauth2/authorize", "keygo")
                .param("client_id", "keygo-ui")
                .param("redirect_uri", "http://localhost:5173/callback")
                .param("scope", "openid profile")
                .param("response_type", "token")
                .param("code_challenge", "abc")
                .param("code_challenge_method", "S256"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.failure.code").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.data.code").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.data.clientMessage").isNotEmpty());
  }

  @Test
  void givenMissingResponseType_whenAuthorize_thenReturns400() throws Exception {
    // Given / When / Then
    mockMvc
        .perform(
            get("/api/v1/tenants/{tenantSlug}/oauth2/authorize", "keygo")
                .param("client_id", "keygo-ui")
                .param("redirect_uri", "http://localhost:5173/callback")
                .param("scope", "openid profile")
                .param("code_challenge", "abc")
                .param("code_challenge_method", "S256"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.failure.code").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.data.code").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.data.clientRequestCause").value("CLIENT_TECHNICAL"))
        .andExpect(jsonPath("$.data.clientMessage").isNotEmpty());
  }
}


