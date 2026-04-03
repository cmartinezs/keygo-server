package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.PasswordRecoveryToken;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.user.entity.PasswordRecoveryTokenEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.repository.PasswordRecoveryTokenJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordRecoveryTokenRepositoryAdapter")
class PasswordRecoveryTokenRepositoryAdapterTest {

  @Mock private PasswordRecoveryTokenJpaRepository jpaRepository;
  @Mock private TenantUserJpaRepository tenantUserJpaRepository;

  private PasswordRecoveryTokenRepositoryAdapter adapter;

  private final UUID userId = UUID.randomUUID();
  private final UUID tenantId = UUID.randomUUID();
  private final String rawToken = "abc123def456abc123def456abc12300";
  private final Instant expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);

  private TenantUserEntity tenantUserEntity;
  private TenantEntity tenantEntity;

  @BeforeEach
  void setUp() {
    adapter = new PasswordRecoveryTokenRepositoryAdapter(jpaRepository, tenantUserJpaRepository);

    tenantEntity = TenantEntity.builder().id(tenantId).build();
    tenantUserEntity = TenantUserEntity.builder()
        .id(userId)
        .tenant(tenantEntity)
        .build();
  }

  // ─── upsert ────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("upsert — inserts new token when none exists for user")
  void upsert_insertsWhenNoExistingToken() {
    // Given
    PasswordRecoveryToken domain = PasswordRecoveryToken.create(
        UserId.of(userId), TenantId.of(tenantId), rawToken, expiresAt);
    when(tenantUserJpaRepository.getReferenceById(userId)).thenReturn(tenantUserEntity);
    when(jpaRepository.findByTenantUser_Id(userId)).thenReturn(Optional.empty());
    PasswordRecoveryTokenEntity saved = PasswordRecoveryTokenEntity.builder()
        .id(UUID.randomUUID()).tenantUser(tenantUserEntity)
        .token(rawToken).expiresAt(expiresAt).createdAt(Instant.now()).build();
    when(jpaRepository.save(any())).thenReturn(saved);

    // When
    PasswordRecoveryToken result = adapter.upsert(domain);

    // Then
    verify(jpaRepository).save(any(PasswordRecoveryTokenEntity.class));
    assertThat(result.getToken()).isEqualTo(rawToken);
  }

  @Test
  @DisplayName("upsert — updates existing token when one already exists")
  void upsert_updatesWhenTokenAlreadyExists() {
    // Given
    PasswordRecoveryToken domain = PasswordRecoveryToken.create(
        UserId.of(userId), TenantId.of(tenantId), rawToken, expiresAt);
    PasswordRecoveryTokenEntity existing = PasswordRecoveryTokenEntity.builder()
        .id(UUID.randomUUID()).tenantUser(tenantUserEntity)
        .token("oldtoken").expiresAt(Instant.now().minus(5, ChronoUnit.MINUTES)).build();
    when(tenantUserJpaRepository.getReferenceById(userId)).thenReturn(tenantUserEntity);
    when(jpaRepository.findByTenantUser_Id(userId)).thenReturn(Optional.of(existing));
    when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    PasswordRecoveryToken result = adapter.upsert(domain);

    // Then
    ArgumentCaptor<PasswordRecoveryTokenEntity> captor =
        ArgumentCaptor.forClass(PasswordRecoveryTokenEntity.class);
    verify(jpaRepository).save(captor.capture());
    assertThat(captor.getValue().getToken()).isEqualTo(rawToken);
    assertThat(captor.getValue().getUsedAt()).isNull();
  }

  // ─── findByToken ────────────────────────────────────────────────────────────

  @Test
  @DisplayName("findByToken — returns domain token when found")
  void findByToken_returnsDomainWhenFound() {
    // Given
    PasswordRecoveryTokenEntity entity = PasswordRecoveryTokenEntity.builder()
        .id(UUID.randomUUID()).tenantUser(tenantUserEntity)
        .token(rawToken).expiresAt(expiresAt).createdAt(Instant.now()).build();
    when(jpaRepository.findByToken(rawToken)).thenReturn(Optional.of(entity));

    // When
    Optional<PasswordRecoveryToken> result = adapter.findByToken(rawToken);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getToken()).isEqualTo(rawToken);
    assertThat(result.get().getTenantId().value()).isEqualTo(tenantId);
  }

  @Test
  @DisplayName("findByToken — returns empty when not found")
  void findByToken_returnsEmptyWhenNotFound() {
    // Given
    when(jpaRepository.findByToken(any())).thenReturn(Optional.empty());

    // When
    Optional<PasswordRecoveryToken> result = adapter.findByToken("unknown");

    // Then
    assertThat(result).isEmpty();
  }

  // ─── markUsed ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("markUsed — calls markUsed on jpaRepository with current timestamp")
  void markUsed_callsRepository() {
    // Given
    PasswordRecoveryToken token = PasswordRecoveryToken.create(
        UserId.of(userId), TenantId.of(tenantId), rawToken, expiresAt);

    // When
    adapter.markUsed(token);

    // Then
    verify(jpaRepository).markUsed(eq(rawToken), any(Instant.class));
  }
}
