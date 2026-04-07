package io.cmartinezs.keygo.supabase.clientapp.mapper;

import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedScope;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.RedirectUri;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAllowedGrantEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAllowedScopeEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientRedirectUriEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper between ClientApp domain model and ClientAppEntity JPA model.
 * <p>Mapper entre el modelo de dominio ClientApp y el modelo JPA ClientAppEntity.
 * @author cmartinezs
 * @version 1.0
 */
public class ClientAppPersistenceMapper {

  /**
   * Convert a domain ClientApp to a JPA ClientAppEntity.
   * <p>Convierte un ClientApp de dominio a un ClientAppEntity JPA.
   * @param clientApp the domain client app
   * @return the corresponding JPA entity
   */
  public ClientAppEntity toEntity(ClientApp clientApp) {
    var builder = ClientAppEntity.builder()
        .clientId(clientApp.getClientId().value())
        .name(clientApp.getName())
        .description(clientApp.getDescription())
        .type(clientApp.getType())
        .hashedSecret(clientApp.getHashedSecret())
        .status(clientApp.getStatus());
    if (clientApp.getId() != null) {
      builder.id(clientApp.getId().value());
    }
    ClientAppEntity entity = builder.build();

    // Map redirect URIs
    List<ClientRedirectUriEntity> uriEntities = clientApp.getRedirectUris().stream()
        .map(uri -> ClientRedirectUriEntity.builder()
            .clientApp(entity)
            .uri(uri.value())
            .build())
        .toList();
    entity.getRedirectUris().clear();
    entity.getRedirectUris().addAll(uriEntities);

    // Map allowed grants
    List<ClientAllowedGrantEntity> grantEntities = clientApp.getAccessPolicy().grants().stream()
        .map(grant -> ClientAllowedGrantEntity.builder()
            .clientApp(entity)
            .grantType(grant)
            .build())
        .toList();
    entity.getAllowedGrants().clear();
    entity.getAllowedGrants().addAll(grantEntities);

    // Map allowed scopes
    List<ClientAllowedScopeEntity> scopeEntities = clientApp.getAccessPolicy().scopes().stream()
        .map(scope -> ClientAllowedScopeEntity.builder()
            .clientApp(entity)
            .scope(scope.value())
            .build())
        .toList();
    entity.getAllowedScopes().clear();
    entity.getAllowedScopes().addAll(scopeEntities);

    return entity;
  }

  /**
   * Convert a JPA ClientAppEntity to a domain ClientApp.
   * <p>Convierte un ClientAppEntity JPA a un ClientApp de dominio.
   * @param entity the JPA entity
   * @return the corresponding domain client app
   */
  public ClientApp toDomain(ClientAppEntity entity) {
    Set<RedirectUri> redirectUris = entity.getRedirectUris().stream()
        .map(u -> RedirectUri.of(u.getUri()))
        .collect(Collectors.toSet());

    Set<AllowedGrant> grants = entity.getAllowedGrants().stream()
        .map(ClientAllowedGrantEntity::getGrantType)
        .collect(Collectors.toSet());

    Set<AllowedScope> scopes = entity.getAllowedScopes().stream()
        .map(s -> AllowedScope.of(s.getScope()))
        .collect(Collectors.toSet());

    AccessPolicy accessPolicy = new AccessPolicy(grants, scopes);

    return ClientApp.builder()
        .id(ClientAppId.of(entity.getId()))
        .tenantId(TenantId.of(entity.getTenant().getId()))
        .clientId(ClientId.of(entity.getClientId()))
        .name(entity.getName())
        .description(entity.getDescription())
        .type(entity.getType())
        .hashedSecret(entity.getHashedSecret())
        .redirectUris(redirectUris)
        .accessPolicy(accessPolicy)
        .status(entity.getStatus())
        .build();
  }
}
