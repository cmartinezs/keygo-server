package io.cmartinezs.keygo.supabase.auth.adapter;

import io.cmartinezs.keygo.app.auth.port.AuthorizationCodeRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.AuthorizationCode;
import io.cmartinezs.keygo.domain.auth.model.AuthorizationCodeId;
import io.cmartinezs.keygo.supabase.auth.mapper.AuthorizationCodePersistenceMapper;
import io.cmartinezs.keygo.supabase.auth.repository.AuthorizationCodeJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adaptador: implementación de AuthorizationCodeRepositoryPort usando JPA.
 */
@Component
public class AuthorizationCodeRepositoryAdapter implements AuthorizationCodeRepositoryPort {

  private final AuthorizationCodeJpaRepository authorizationCodeJpaRepository;
  private final ClientAppJpaRepository clientAppJpaRepository;
  private final TenantJpaRepository tenantJpaRepository;
  private final TenantUserJpaRepository userJpaRepository;

  public AuthorizationCodeRepositoryAdapter(
      AuthorizationCodeJpaRepository authorizationCodeJpaRepository,
      ClientAppJpaRepository clientAppJpaRepository,
      TenantJpaRepository tenantJpaRepository,
      TenantUserJpaRepository userJpaRepository) {
    this.authorizationCodeJpaRepository = authorizationCodeJpaRepository;
    this.clientAppJpaRepository = clientAppJpaRepository;
    this.tenantJpaRepository = tenantJpaRepository;
    this.userJpaRepository = userJpaRepository;
  }

  @Override
  public AuthorizationCode save(AuthorizationCode authorizationCode) {
    var clientAppEntity =
        clientAppJpaRepository
            .findById(authorizationCode.getClientAppId().value())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "ClientApp not found: "
                            + authorizationCode.getClientAppId().value()));

    var tenantEntity =
        tenantJpaRepository
            .findById(authorizationCode.getTenantId().value())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Tenant not found: " + authorizationCode.getTenantId().value()));

    var userEntity =
        userJpaRepository
            .findById(authorizationCode.getUserId().value())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "User not found: " + authorizationCode.getUserId().value()));

    var entity =
        AuthorizationCodePersistenceMapper.toEntity(
            authorizationCode, clientAppEntity, tenantEntity, userEntity);
    var savedEntity = authorizationCodeJpaRepository.save(entity);
    return AuthorizationCodePersistenceMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<AuthorizationCode> findByCode(String code) {
    return authorizationCodeJpaRepository.findByCode(code)
        .map(AuthorizationCodePersistenceMapper::toDomain);
  }

  @Override
  public Optional<AuthorizationCode> findById(AuthorizationCodeId authorizationCodeId) {
    return authorizationCodeJpaRepository
        .findById(authorizationCodeId.id())
        .map(AuthorizationCodePersistenceMapper::toDomain);
  }

  @Override
  public AuthorizationCode update(AuthorizationCode authorizationCode) {
    var entity =
        authorizationCodeJpaRepository
            .findById(authorizationCode.getId().id())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "AuthorizationCode not found: "
                            + authorizationCode.getId().id()));

    entity.setStatus(authorizationCode.getStatus().getValue());
    entity.setUsedAt(authorizationCode.getUsedAt());

    var updatedEntity = authorizationCodeJpaRepository.save(entity);
    return AuthorizationCodePersistenceMapper.toDomain(updatedEntity);
  }
}




