package io.cmartinezs.keygo.app.platform.usecase;

import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.result.IssueTokensResult;
import io.cmartinezs.keygo.app.auth.usecase.IssueTokensUseCase;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.app.platform.result.IssuePlatformTokensResult;
import io.cmartinezs.keygo.domain.auth.model.RefreshToken;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssuePlatformTokensUseCaseTest {

  @Mock PlatformUserRoleRepositoryPort platformUserRoleRepository;
  @Mock IssueTokensUseCase issueTokensUseCase;
  @Mock SessionRepositoryPort sessionRepository;
  @Mock RefreshTokenRepositoryPort refreshTokenRepository;
  @Mock ClockPort clock;

  IssuePlatformTokensUseCase useCase;

  private PlatformUser platformUser;
  private final UUID userId = UUID.randomUUID();
  private final Instant now = Instant.parse("2026-04-10T12:00:00Z");

  @BeforeEach
  void setUp() {
    useCase = new IssuePlatformTokensUseCase(
        platformUserRoleRepository, issueTokensUseCase,
        sessionRepository, refreshTokenRepository, clock);

    platformUser = PlatformUser.builder()
        .id(UserId.of(userId))
        .username(Username.of("platform_admin"))
        .email(EmailAddress.of("admin@keygo.local"))
        .passwordHash(PasswordHash.of("$2a$10$hashed"))
        .firstName("Admin")
        .lastName("KeyGo")
        .status(UserStatus.ACTIVE)
        .build();
  }

  @Test
  void givenValidUser_whenExecute_thenReturnsTokens() {
    // Given
    when(clock.now()).thenReturn(now);
    when(platformUserRoleRepository.findRoleCodesByPlatformUserId(userId))
        .thenReturn(List.of("keygo_admin", "keygo_user"));
    when(issueTokensUseCase.execute(
        isNull(), eq("http://localhost/keygo-server/api/v1/platform"),
        eq(userId.toString()), eq("keygo-platform"),
        eq("openid profile platform"), isNull(),
        eq("admin@keygo.local"), eq("Admin KeyGo"),
        isNull(), eq(List.of("keygo_admin", "keygo_user"))))
        .thenReturn(new IssueTokensResult(
            "access.jwt", "id.jwt", "Bearer", 3600L,
            "openid profile platform", null, "signing-key-1"));
    when(sessionRepository.save(any(Session.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // When
    IssuePlatformTokensResult result = useCase.execute(
        platformUser,
        "http://localhost/keygo-server/api/v1/platform",
        "Mozilla/5.0", "127.0.0.1");

    // Then
    assertThat(result.accessToken()).isEqualTo("access.jwt");
    assertThat(result.refreshToken()).isNotBlank();
    assertThat(result.tokenType()).isEqualTo("Bearer");
    assertThat(result.expiresIn()).isEqualTo(3600L);
    assertThat(result.signingKeyId()).isEqualTo("signing-key-1");

    // Verify session saved with null clientAppId (platform session)
    ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
    verify(sessionRepository).save(sessionCaptor.capture());
    Session savedSession = sessionCaptor.getValue();
    assertThat(savedSession.isPlatformSession()).isTrue();
    assertThat(savedSession.getPlatformUserId()).isEqualTo(userId);
    assertThat(savedSession.getUserAgent()).isEqualTo("Mozilla/5.0");
    assertThat(savedSession.getIpAddress()).isEqualTo("127.0.0.1");

    // Verify refresh token saved with null clientAppId and null tenantUserId
    ArgumentCaptor<RefreshToken> rtCaptor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenRepository).save(rtCaptor.capture());
    RefreshToken savedRT = rtCaptor.getValue();
    assertThat(savedRT.isPlatformToken()).isTrue();
    assertThat(savedRT.getTenantUserId()).isNull();
    assertThat(savedRT.getScopes()).isEqualTo("openid profile platform");
  }

  @Test
  void givenUserWithNoRoles_whenExecute_thenReturnsTokensWithEmptyRoles() {
    // Given
    when(clock.now()).thenReturn(now);
    when(platformUserRoleRepository.findRoleCodesByPlatformUserId(userId))
        .thenReturn(List.of());
    when(issueTokensUseCase.execute(
        isNull(), anyString(), anyString(), eq("keygo-platform"),
        eq("openid profile platform"), isNull(),
        anyString(), anyString(), isNull(), eq(List.of())))
        .thenReturn(new IssueTokensResult(
            "access.jwt", "id.jwt", "Bearer", 3600L,
            "openid profile platform", null, "signing-key-2"));
    when(sessionRepository.save(any(Session.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // When
    IssuePlatformTokensResult result = useCase.execute(
        platformUser, "http://localhost/api/v1/platform", "curl/8.0", "10.0.0.1");

    // Then
    assertThat(result.accessToken()).isEqualTo("access.jwt");
    assertThat(result.tokenType()).isEqualTo("Bearer");
    verify(platformUserRoleRepository).findRoleCodesByPlatformUserId(userId);
  }

  @Test
  void givenUserWithNullNames_whenExecute_thenNameIsNull() {
    // Given
    PlatformUser userNoName = PlatformUser.builder()
        .id(UserId.of(userId))
        .username(Username.of("anon"))
        .email(EmailAddress.of("anon@keygo.local"))
        .passwordHash(PasswordHash.of("$2a$10$hashed"))
        .status(UserStatus.ACTIVE)
        .build();

    when(clock.now()).thenReturn(now);
    when(platformUserRoleRepository.findRoleCodesByPlatformUserId(userId))
        .thenReturn(List.of());
    when(issueTokensUseCase.execute(
        isNull(), anyString(), anyString(), eq("keygo-platform"),
        eq("openid profile platform"), isNull(),
        eq("anon@keygo.local"), isNull(),
        isNull(), anyList()))
        .thenReturn(new IssueTokensResult(
            "access.jwt", "id.jwt", "Bearer", 3600L,
            "openid profile platform", null, "key-3"));
    when(sessionRepository.save(any(Session.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // When
    IssuePlatformTokensResult result = useCase.execute(
        userNoName, "http://localhost/api/v1/platform", null, null);

    // Then
    assertThat(result.accessToken()).isEqualTo("access.jwt");
  }
}
