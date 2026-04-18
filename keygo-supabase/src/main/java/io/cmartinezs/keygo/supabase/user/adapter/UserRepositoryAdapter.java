package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.user.filter.UserFilter;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.mapper.UserPersistenceMapper;
import io.cmartinezs.keygo.supabase.user.repository.PlatformUserJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing UserRepositoryPort using Spring Data JPA.
 * <p>Adaptador que implementa UserRepositoryPort usando Spring Data JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

  private final TenantUserJpaRepository tenantUserJpaRepository;
  private final PlatformUserJpaRepository platformUserJpaRepository;
  private final TenantJpaRepository tenantJpaRepository;
  private final UserPersistenceMapper mapper;

  public UserRepositoryAdapter(
      TenantUserJpaRepository tenantUserJpaRepository,
      PlatformUserJpaRepository platformUserJpaRepository,
      TenantJpaRepository tenantJpaRepository) {
    this.tenantUserJpaRepository = tenantUserJpaRepository;
    this.platformUserJpaRepository = platformUserJpaRepository;
    this.tenantJpaRepository = tenantJpaRepository;
    this.mapper = new UserPersistenceMapper();
  }

  @Override
  public User save(User user) {
    TenantUserEntity entity =
        user.getId() != null
            ? tenantUserJpaRepository
                .findById(user.getId().value())
                .orElseGet(TenantUserEntity::new)
            : new TenantUserEntity();

    PlatformUserEntity platformUser = platformUserJpaRepository.save(resolvePlatformUser(entity, user));

    entity.setTenant(tenantJpaRepository.getReferenceById(user.getTenantId().value()));
    entity.setPlatformUser(platformUser);
    entity.setLocalUsername(user.getUsername().value());

    if (user.getStatus() != null && user.getStatus() != UserStatus.RESET_PASSWORD) {
      entity.setStatus(user.getStatus());
    } else if (entity.getStatus() == null) {
      entity.setStatus(UserStatus.ACTIVE);
    }

    return mapper.toDomain(tenantUserJpaRepository.save(entity));
  }

  @Override
  public Optional<User> findByIdAndTenantId(UserId userId, TenantId tenantId) {
    return tenantUserJpaRepository.findByIdAndTenantId(userId.value(), tenantId.value())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<User> findById(UserId userId) {
    return tenantUserJpaRepository.findById(userId.value())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<User> findByTenantIdAndEmail(TenantId tenantId, EmailAddress email) {
    return tenantUserJpaRepository.findByTenantIdAndEmail(tenantId.value(), email.value())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<User> findByTenantIdAndUsername(TenantId tenantId, Username username) {
    return tenantUserJpaRepository.findByTenantIdAndUsername(tenantId.value(), username.value())
        .map(mapper::toDomain);
  }

  public Optional<User> findByTenantIdAndPlatformUserId(TenantId tenantId, UserId platformUserId) {
    return tenantUserJpaRepository.findByTenantIdAndPlatformUserId(tenantId.value(), platformUserId.value())
        .map(mapper::toDomain);
  }

  @Override
  public boolean existsByTenantIdAndEmail(TenantId tenantId, EmailAddress email) {
    return tenantUserJpaRepository.existsByTenantIdAndEmail(tenantId.value(), email.value());
  }

  @Override
  public boolean existsByTenantIdAndUsername(TenantId tenantId, Username username) {
    return tenantUserJpaRepository.existsByTenantIdAndLocalUsername(tenantId.value(), username.value());
  }

  @Override
  public List<Username> findUsernamesByPrefix(TenantId tenantId, String prefix) {
    return tenantUserJpaRepository
        .findUsernamesByPrefix(tenantId.value(), prefix.toLowerCase())
        .stream()
        .map(Username::of)
        .toList();
  }

  @Override
  public List<User> findAllByTenantId(TenantId tenantId) {
    return tenantUserJpaRepository.findAllByTenantId(tenantId.value()).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public PagedResult<User> findAllPaged(TenantId tenantId, UserFilter filter) {
    Specification<TenantUserEntity> spec = buildSpecification(tenantId, filter);
    PageRequest pageRequest = buildPageRequest(filter);

    Page<TenantUserEntity> page = tenantUserJpaRepository.findAll(spec, pageRequest);

    List<User> users = page.getContent().stream()
        .map(mapper::toDomain)
        .toList();

    return PagedResult.of(users, page.getNumber(), page.getSize(), page.getTotalElements());
  }

  private Specification<TenantUserEntity> buildSpecification(
      TenantId tenantId, UserFilter filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Filter by tenantId (scope)
      predicates.add(cb.equal(root.get("tenant").get("id"), tenantId.value()));

      // Filter by status
      if (filter.hasStatus()) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      // Filter by username (case-insensitive LIKE)
      if (filter.hasUsernameLike()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("localUsername")),
                "%" + filter.getUsernameLike().toLowerCase() + "%"));
      }

      // Filter by email (case-insensitive LIKE)
      if (filter.hasEmailLike()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("platformUser").get("email")),
                "%" + filter.getEmailLike().toLowerCase() + "%"));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private PageRequest buildPageRequest(UserFilter filter) {
    if (filter.hasSorting()) {
      Sort.Direction direction = Sort.Direction.fromString(filter.getSortOrder());
      return PageRequest.of(filter.getPage(), filter.getSize(), Sort.by(direction, filter.getSortBy()));
    }
    return PageRequest.of(filter.getPage(), filter.getSize());
  }

  private PlatformUserEntity resolvePlatformUser(TenantUserEntity entity, User user) {
    PlatformUserEntity platformUser =
        entity.getPlatformUser() != null
            ? entity.getPlatformUser()
            : resolvePlatformUserReference(user);

    platformUser.setEmail(user.getEmail().value());
    platformUser.setPasswordHash(user.getPasswordHash().value());
    platformUser.setFirstName(user.getFirstName());
    platformUser.setLastName(user.getLastName());
    platformUser.setDisplayName(resolveDisplayName(user));
    platformUser.setPhoneNumber(user.getPhoneNumber());
    platformUser.setLocale(user.getLocale());
    platformUser.setZoneinfo(user.getZoneinfo());
    platformUser.setProfilePictureUrl(user.getProfilePictureUrl());
    platformUser.setBirthdate(parseBirthdate(user.getBirthdate()));
    platformUser.setWebsite(user.getWebsite());
    platformUser.setStatus(user.getStatus().name());

    return platformUser;
  }

  private PlatformUserEntity resolvePlatformUserReference(User user) {
    if (user.getPlatformUserId() != null) {
      return platformUserJpaRepository
          .findById(user.getPlatformUserId())
          .orElseThrow(
              () ->
                  new IllegalArgumentException(
                      "PlatformUser not found: " + user.getPlatformUserId()));
    }

    return platformUserJpaRepository
        .findByEmail(user.getEmail().value())
        .orElseGet(PlatformUserEntity::new);
  }

  private LocalDate parseBirthdate(String birthdate) {
    return birthdate != null && !birthdate.isBlank() ? LocalDate.parse(birthdate) : null;
  }

  private String resolveDisplayName(User user) {
    if (user.getFirstName() != null && user.getLastName() != null) {
      return (user.getFirstName() + " " + user.getLastName()).trim();
    }
    if (user.getFirstName() != null) {
      return user.getFirstName();
    }
    if (user.getLastName() != null) {
      return user.getLastName();
    }
    return user.getUsername().value();
  }
}
