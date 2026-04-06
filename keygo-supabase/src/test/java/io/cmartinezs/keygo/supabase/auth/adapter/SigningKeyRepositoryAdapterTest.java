package io.cmartinezs.keygo.supabase.auth.adapter;

import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyStatus;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import io.cmartinezs.keygo.supabase.auth.repository.SigningKeyJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SigningKeyRepositoryAdapterTest {

  @Mock SigningKeyJpaRepository jpaRepository;
  @Mock TenantJpaRepository tenantJpaRepository;

  SigningKeyRepositoryAdapter adapter;

  private SigningKeyEntity buildEntity(String status) {
    return SigningKeyEntity.builder()
        .id(UUID.randomUUID())
        .kid("kid-" + status)
        .algorithm("RS256")
        .status(status)
        .publicMaterial("PUBLIC_PEM")
        .privateMaterial("PRIVATE_PEM")
        .activatedAt(Instant.now())
        .build();
  }

  @BeforeEach
  void setUp() {
    adapter = new SigningKeyRepositoryAdapter(jpaRepository, tenantJpaRepository);
  }

  @Test
  void givenActiveKey_whenFindActiveKey_thenReturnsDomain() {
    // Given
    var entity = buildEntity("ACTIVE");
    when(jpaRepository.findFirstByTenantIsNullAndStatus("ACTIVE")).thenReturn(Optional.of(entity));

    // When
    Optional<SigningKey> result = adapter.findActiveKey();

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getStatus()).isEqualTo(SigningKeyStatus.ACTIVE);
    assertThat(result.get().getKid()).isEqualTo("kid-ACTIVE");
  }

  @Test
  void givenNoActiveKey_whenFindActiveKey_thenReturnsEmpty() {
    // Given
    when(jpaRepository.findFirstByTenantIsNullAndStatus("ACTIVE")).thenReturn(Optional.empty());

    // When / Then
    assertThat(adapter.findActiveKey()).isEmpty();
  }

  @Test
  void givenActiveAndRetiredKeys_whenFindPublishableKeys_thenReturnsBoth() {
    // Given
    var active = buildEntity("ACTIVE");
    var retired = buildEntity("RETIRED");
    when(jpaRepository.findByStatusIn(List.of("ACTIVE", "RETIRED")))
        .thenReturn(List.of(active, retired));

    // When
    List<SigningKey> result = adapter.findPublishableKeys();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.stream().map(SigningKey::getStatus))
        .containsExactlyInAnyOrder(SigningKeyStatus.ACTIVE, SigningKeyStatus.RETIRED);
  }
}

