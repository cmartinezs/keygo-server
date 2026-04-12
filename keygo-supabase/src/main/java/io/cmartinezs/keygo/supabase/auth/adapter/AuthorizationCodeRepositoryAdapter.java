package io.cmartinezs.keygo.supabase.auth.adapter;

import io.cmartinezs.keygo.app.auth.port.AuthorizationCodeRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.AuthorizationCode;
import io.cmartinezs.keygo.domain.auth.model.AuthorizationCodeId;
import io.cmartinezs.keygo.supabase.auth.entity.PlatformSessionEntity;
import io.cmartinezs.keygo.supabase.auth.mapper.AuthorizationCodePersistenceMapper;
import io.cmartinezs.keygo.supabase.auth.repository.AuthorizationCodeJpaRepository;
import io.cmartinezs.keygo.supabase.auth.repository.PlatformSessionJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationCodeRepositoryAdapter implements AuthorizationCodeRepositoryPort {

  private final AuthorizationCodeJpaRepository authorizationCodeJpaRepository;
  private final PlatformSessionJpaRepository platformSessionJpaRepository;
  private final ClientAppJpaRepository clientAppJpaRepository;
  private final TenantUserJpaRepository userJpaRepository;

  public AuthorizationCodeRepositoryAdapter(
      AuthorizationCodeJpaRepository authorizationCodeJpaRepository,
      PlatformSessionJpaRepository platformSessionJpaRepository,
      ClientAppJpaRepository clientAppJpaRepository,
      TenantUserJpaRepository userJpaRepository) {
    this.authorizationCodeJpaRepository = authorizationCodeJpaRepository;
    this.platformSessionJpaRepository = platformSessionJpaRepository;
    this.clientAppJpaRepository = clientAppJpaRepository;
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
                        "ClientApp not found: " + authorizationCode.getClientAppId().value()));

    TenantUserEntity userEntity =
        userJpaRepository
            .findById(authorizationCode.getUserId().value())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "User not found: " + authorizationCode.getUserId().value()));
    PlatformUserEntity platformUserEntity = userEntity.getPlatformUser();

    PlatformSessionEntity platformSessionEntity =
        PlatformSessionEntity.builder()
            .id(authorizationCode.getId().id())
            .platformUser(platformUserEntity)
            .status("ACTIVE")
            .expiresAt(authorizationCode.getExpiresAt())
            .lastAccessedAt(Instant.now())
            .startedAt(Instant.now())
            .build();

    var entity =
        AuthorizationCodePersistenceMapper.toEntity(
            authorizationCode,
            sha256Hex(authorizationCode.getCode()),
            platformSessionJpaRepository.save(platformSessionEntity),
            platformUserEntity,
            clientAppEntity,
            userEntity);

    return AuthorizationCodePersistenceMapper.toDomain(
        authorizationCodeJpaRepository.save(entity), authorizationCode.getCode());
  }

  @Override
  public Optional<AuthorizationCode> findByCode(String code) {
    return authorizationCodeJpaRepository.findByCodeHash(sha256Hex(code))
        .map(entity -> AuthorizationCodePersistenceMapper.toDomain(entity, code));
  }

  @Override
  public Optional<AuthorizationCode> findById(AuthorizationCodeId authorizationCodeId) {
    return authorizationCodeJpaRepository.findById(authorizationCodeId.id())
        .map(entity -> AuthorizationCodePersistenceMapper.toDomain(entity, null));
  }

  @Override
  public AuthorizationCode update(AuthorizationCode authorizationCode) {
    var entity =
        authorizationCodeJpaRepository
            .findById(authorizationCode.getId().id())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "AuthorizationCode not found: " + authorizationCode.getId().id()));

    entity.setStatus(authorizationCode.getStatus().getValue());
    entity.setUsedAt(authorizationCode.getUsedAt());

    return AuthorizationCodePersistenceMapper.toDomain(
        authorizationCodeJpaRepository.save(entity), authorizationCode.getCode());
  }

  private String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder result = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        result.append(String.format("%02x", b));
      }
      return result.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
