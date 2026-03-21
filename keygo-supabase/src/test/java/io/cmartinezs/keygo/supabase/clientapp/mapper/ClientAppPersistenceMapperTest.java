package io.cmartinezs.keygo.supabase.clientapp.mapper;

import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAllowedGrantEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAllowedScopeEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientRedirectUriEntity;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClientAppPersistenceMapperTest {

  private ClientAppPersistenceMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ClientAppPersistenceMapper();
  }

  private ClientApp buildDomainApp() {
    return ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(TenantId.generate())
        .clientId(ClientId.of("my-client"))
        .name("My App")
        .description("Description")
        .type(ClientType.CONFIDENTIAL)
        .hashedSecret("$2a$hashed")
        .redirectUris(Set.of())
        .accessPolicy(new AccessPolicy(
            Set.of(AllowedGrant.AUTHORIZATION_CODE, AllowedGrant.REFRESH_TOKEN),
            Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  private ClientAppEntity buildEntity() {
    TenantEntity tenantRef = new TenantEntity();
    tenantRef.setId(UUID.randomUUID());

    ClientAllowedGrantEntity grantEntity = ClientAllowedGrantEntity.builder()
        .id(UUID.randomUUID())
        .grantType(AllowedGrant.AUTHORIZATION_CODE)
        .build();

    ClientAllowedScopeEntity scopeEntity = ClientAllowedScopeEntity.builder()
        .id(UUID.randomUUID())
        .scope("openid")
        .build();

    ClientRedirectUriEntity uriEntity = ClientRedirectUriEntity.builder()
        .id(UUID.randomUUID())
        .uri("https://example.com/callback")
        .build();

    ClientAppEntity entity = ClientAppEntity.builder()
        .id(UUID.randomUUID())
        .tenant(tenantRef)
        .clientId("my-client")
        .name("My App")
        .type(ClientType.CONFIDENTIAL)
        .hashedSecret("$2a$hashed")
        .status(ClientAppStatus.ACTIVE)
        .allowedGrants(new ArrayList<>(List.of(grantEntity)))
        .allowedScopes(new ArrayList<>(List.of(scopeEntity)))
        .redirectUris(new ArrayList<>(List.of(uriEntity)))
        .build();

    grantEntity.setClientApp(entity);
    scopeEntity.setClientApp(entity);
    uriEntity.setClientApp(entity);
    return entity;
  }

  @Test
  void toEntity_shouldMapDomainFieldsCorrectly() {
    // Given
    ClientApp domain = buildDomainApp();

    // When
    ClientAppEntity entity = mapper.toEntity(domain);

    // Then
    assertThat(entity.getId()).isEqualTo(domain.getId().value());
    assertThat(entity.getClientId()).isEqualTo("my-client");
    assertThat(entity.getName()).isEqualTo("My App");
    assertThat(entity.getType()).isEqualTo(ClientType.CONFIDENTIAL);
    assertThat(entity.getHashedSecret()).isEqualTo("$2a$hashed");
    assertThat(entity.getStatus()).isEqualTo(ClientAppStatus.ACTIVE);
    assertThat(entity.getAllowedGrants()).hasSize(2);
  }

  @Test
  void toDomain_shouldMapEntityFieldsCorrectly() {
    // Given
    ClientAppEntity entity = buildEntity();

    // When
    ClientApp domain = mapper.toDomain(entity);

    // Then
    assertThat(domain.getId().value()).isEqualTo(entity.getId());
    assertThat(domain.getClientId().value()).isEqualTo("my-client");
    assertThat(domain.getName()).isEqualTo("My App");
    assertThat(domain.getType()).isEqualTo(ClientType.CONFIDENTIAL);
    assertThat(domain.getAccessPolicy().grants()).contains(AllowedGrant.AUTHORIZATION_CODE);
    assertThat(domain.getAccessPolicy().scopes()).hasSize(1);
    assertThat(domain.getRedirectUris()).hasSize(1);
  }
}

