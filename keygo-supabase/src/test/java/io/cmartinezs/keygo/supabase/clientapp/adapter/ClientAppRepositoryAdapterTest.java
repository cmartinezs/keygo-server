package io.cmartinezs.keygo.supabase.clientapp.adapter;

import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAllowedGrantEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAllowedScopeEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientRedirectUriEntity;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.cmartinezs.keygo.domain.clientapp.model.ClientType.CONFIDENTIAL;
import static io.cmartinezs.keygo.domain.tenant.model.TenantStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientAppRepositoryAdapterTest {

  @Mock private ClientAppJpaRepository jpaRepository;

  private ClientAppRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new ClientAppRepositoryAdapter(jpaRepository);
  }

  @Test
  void givenClientAppEntity_whenFindByClientIdAndTenantId_thenMapsAggregatesToDomain() {
    // Given
    UUID tenantUuid = UUID.randomUUID();
    UUID clientAppUuid = UUID.randomUUID();

    TenantEntity tenant = TenantEntity.builder()
        .id(tenantUuid)
        .slug("acme")
        .name("Acme")
        .ownerEmail("owner@acme.test")
        .status(ACTIVE)
        .build();

    ClientAppEntity entity = ClientAppEntity.builder()
        .id(clientAppUuid)
        .tenant(tenant)
        .clientId("acme-web")
        .name("Acme Web")
        .description("Web app")
        .type(CONFIDENTIAL)
        .hashedSecret("hashed-secret")
        .status(io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus.ACTIVE)
        .build();

    entity.getRedirectUris().add(ClientRedirectUriEntity.builder()
        .id(UUID.randomUUID())
        .clientApp(entity)
        .uri("https://acme.test/callback")
        .build());

    entity.getAllowedGrants().add(ClientAllowedGrantEntity.builder()
        .id(UUID.randomUUID())
        .clientApp(entity)
        .grantType(AllowedGrant.AUTHORIZATION_CODE)
        .build());

    entity.getAllowedScopes().add(ClientAllowedScopeEntity.builder()
        .id(UUID.randomUUID())
        .clientApp(entity)
        .scope("openid")
        .build());

    when(jpaRepository.findByClientIdAndTenantId("acme-web", tenantUuid))
        .thenReturn(Optional.of(entity));

    // When
    Optional<io.cmartinezs.keygo.domain.clientapp.model.ClientApp> result =
        adapter.findByClientIdAndTenantId(ClientId.of("acme-web"), TenantId.of(tenantUuid));

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getTenantId().value()).isEqualTo(tenantUuid);
    assertThat(result.get().getRedirectUris())
        .extracting(io.cmartinezs.keygo.domain.clientapp.model.RedirectUri::value)
        .containsExactly("https://acme.test/callback");
    assertThat(result.get().getAccessPolicy().grants())
        .containsExactly(AllowedGrant.AUTHORIZATION_CODE);
    assertThat(result.get().getAccessPolicy().scopes())
        .extracting(io.cmartinezs.keygo.domain.clientapp.model.AllowedScope::value)
        .containsExactly("openid");
  }

  @Test
  void givenNoClientAppEntity_whenFindByClientIdAndTenantId_thenReturnsEmpty() {
    // Given
    UUID tenantUuid = UUID.randomUUID();
    when(jpaRepository.findByClientIdAndTenantId("acme-web", tenantUuid)).thenReturn(Optional.empty());

    // When
    Optional<io.cmartinezs.keygo.domain.clientapp.model.ClientApp> result =
        adapter.findByClientIdAndTenantId(ClientId.of("acme-web"), TenantId.of(tenantUuid));

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void givenTenantId_whenFindAllByTenantId_thenMapsAllEntities() {
    // Given
    UUID tenantUuid = UUID.randomUUID();
    TenantEntity tenant = TenantEntity.builder()
        .id(tenantUuid)
        .slug("acme")
        .name("Acme")
        .ownerEmail("owner@acme.test")
        .status(ACTIVE)
        .build();

    ClientAppEntity first = ClientAppEntity.builder()
        .id(UUID.randomUUID())
        .tenant(tenant)
        .clientId("acme-web")
        .name("Acme Web")
        .type(CONFIDENTIAL)
        .hashedSecret("hashed-secret-1")
        .status(io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus.ACTIVE)
        .build();
    first.getAllowedGrants().add(ClientAllowedGrantEntity.builder()
        .id(UUID.randomUUID())
        .clientApp(first)
        .grantType(AllowedGrant.AUTHORIZATION_CODE)
        .build());

    ClientAppEntity second = ClientAppEntity.builder()
        .id(UUID.randomUUID())
        .tenant(tenant)
        .clientId("acme-admin")
        .name("Acme Admin")
        .type(CONFIDENTIAL)
        .hashedSecret("hashed-secret-2")
        .status(io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus.ACTIVE)
        .build();
    second.getAllowedGrants().add(ClientAllowedGrantEntity.builder()
        .id(UUID.randomUUID())
        .clientApp(second)
        .grantType(AllowedGrant.AUTHORIZATION_CODE)
        .build());

    when(jpaRepository.findAllByTenantId(tenantUuid)).thenReturn(List.of(first, second));

    // When
    var result = adapter.findAllByTenantId(TenantId.of(tenantUuid));

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).extracting(io.cmartinezs.keygo.domain.clientapp.model.ClientApp::getClientId)
        .extracting(io.cmartinezs.keygo.domain.clientapp.model.ClientId::value)
        .containsExactlyInAnyOrder("acme-web", "acme-admin");
  }
}



