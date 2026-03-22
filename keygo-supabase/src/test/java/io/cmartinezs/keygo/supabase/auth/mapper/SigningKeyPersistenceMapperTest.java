package io.cmartinezs.keygo.supabase.auth.mapper;

import io.cmartinezs.keygo.domain.auth.model.SigningKeyAlgorithm;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyId;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyStatus;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.supabase.auth.entity.SigningKeyEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SigningKeyPersistenceMapperTest {

  @Test
  void givenEntity_whenToDomain_thenMapsAllFields() {
    // Given
    UUID id = UUID.randomUUID();
    SigningKeyEntity entity = SigningKeyEntity.builder()
        .id(id)
        .kid("kid-test")
        .algorithm("RS256")
        .status("ACTIVE")
        .publicMaterial("PUBLIC_PEM")
        .privateMaterial("PRIVATE_PEM")
        .activatedAt(Instant.now())
        .build();

    // When
    SigningKey domain = SigningKeyPersistenceMapper.toDomain(entity);

    // Then
    assertThat(domain.getId().value()).isEqualTo(id.toString());
    assertThat(domain.getKid()).isEqualTo("kid-test");
    assertThat(domain.getAlgorithm()).isEqualTo(SigningKeyAlgorithm.RS256);
    assertThat(domain.getStatus()).isEqualTo(SigningKeyStatus.ACTIVE);
    assertThat(domain.getPublicMaterial()).isEqualTo("PUBLIC_PEM");
    assertThat(domain.getPrivateMaterial()).isEqualTo("PRIVATE_PEM");
  }

  @Test
  void givenDomain_whenToEntity_thenMapsAllFields() {
    // Given
    SigningKey domain = SigningKey.builder()
        .id(new SigningKeyId(UUID.randomUUID().toString()))
        .kid("kid-42")
        .algorithm(SigningKeyAlgorithm.RS256)
        .status(SigningKeyStatus.RETIRED)
        .publicMaterial("PUB")
        .privateMaterial(null)
        .activatedAt(Instant.now())
        .build();

    // When
    SigningKeyEntity entity = SigningKeyPersistenceMapper.toEntity(domain);

    // Then
    assertThat(entity.getKid()).isEqualTo("kid-42");
    assertThat(entity.getAlgorithm()).isEqualTo("RS256");
    assertThat(entity.getStatus()).isEqualTo("RETIRED");
    assertThat(entity.getPublicMaterial()).isEqualTo("PUB");
    assertThat(entity.getPrivateMaterial()).isNull();
  }
}

