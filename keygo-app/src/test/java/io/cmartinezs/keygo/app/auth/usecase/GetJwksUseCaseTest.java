package io.cmartinezs.keygo.app.auth.usecase;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.cmartinezs.keygo.app.auth.port.JwksBuilderPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyAlgorithm;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyId;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetJwksUseCaseTest {

  @Mock SigningKeyRepositoryPort signingKeyRepository;
  @Mock JwksBuilderPort jwksBuilder;
  @Mock TenantRepositoryPort tenantRepository;

  GetJwksUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetJwksUseCase(signingKeyRepository, jwksBuilder, tenantRepository);
  }

  @Test
  void givenPublishableKeys_whenExecute_thenReturnJwkSet() {
    // Given
    var key =
        SigningKey.builder()
            .id(new SigningKeyId("k1"))
            .kid("kid-1")
            .algorithm(SigningKeyAlgorithm.RS256)
            .status(SigningKeyStatus.ACTIVE)
            .publicMaterial("PUBLIC_PEM")
            .activatedAt(Instant.now())
            .build();
    var expected = Map.<String, Object>of("keys", List.of(Map.of("kid", "kid-1")));
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(key));
    when(jwksBuilder.buildJwkSet(List.of(key))).thenReturn(expected);

    // When
    var result = useCase.execute();

    // Then
    assertThat(result).containsKey("keys").containsEntry("keys", List.of(Map.of("kid", "kid-1")));
  }

  @Test
  void givenNoPublishableKeys_whenExecute_thenReturnEmptyKeys() {
    // Given
    var expected = Map.<String, Object>of("keys", List.of());
    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of());
    when(jwksBuilder.buildJwkSet(List.of())).thenReturn(expected);

    // When
    var result = useCase.execute();

    // Then
    assertThat(result).containsEntry("keys", List.of());
  }
}
