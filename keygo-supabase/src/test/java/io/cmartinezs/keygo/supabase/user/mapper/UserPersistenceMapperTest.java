package io.cmartinezs.keygo.supabase.user.mapper;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserPersistenceMapperTest {

  private static final String USERNAME = "johndoe";
  private static final String EMAIL = "john@acme.com";
  private static final String HASH = "$2a$10$testhash";

  private UserPersistenceMapper mapper;
  private UUID tenantUuid;
  private UUID userUuid;

  @BeforeEach
  void setUp() {
    mapper = new UserPersistenceMapper();
    tenantUuid = UUID.randomUUID();
    userUuid = UUID.randomUUID();
  }

  @Test
  void toDomainMapsAllFields() {
    // Given
    TenantEntity tenantEntity = TenantEntity.builder().id(tenantUuid).build();
    TenantUserEntity entity = TenantUserEntity.builder()
        .id(userUuid)
        .tenant(tenantEntity)
        .username(USERNAME)
        .email(EMAIL)
        .passwordHash(HASH)
        .firstName("John")
        .lastName("Doe")
        .status(UserStatus.ACTIVE)
        .build();

    // When
    User user = mapper.toDomain(entity);

    // Then
    assertThat(user.getId().value()).isEqualTo(userUuid);
    assertThat(user.getTenantId().value()).isEqualTo(tenantUuid);
    assertThat(user.getUsername().value()).isEqualTo(USERNAME);
    assertThat(user.getEmail().value()).isEqualTo(EMAIL);
    assertThat(user.getPasswordHash().value()).isEqualTo(HASH);
    assertThat(user.getFirstName()).isEqualTo("John");
    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
  }

  @Test
  void toEntityMapsAllFields() {
    // Given
    User user = User.builder()
        .id(UserId.of(userUuid))
        .tenantId(TenantId.of(tenantUuid))
        .username(Username.of(USERNAME))
        .email(EmailAddress.of(EMAIL))
        .passwordHash(PasswordHash.of(HASH))
        .firstName("Jane")
        .lastName("Smith")
        .status(UserStatus.ACTIVE)
        .build();

    // When
    TenantUserEntity entity = mapper.toEntity(user);

    // Then
    assertThat(entity.getId()).isEqualTo(userUuid);
    assertThat(entity.getTenant().getId()).isEqualTo(tenantUuid);
    assertThat(entity.getUsername()).isEqualTo(USERNAME);
    assertThat(entity.getEmail()).isEqualTo(EMAIL);
    assertThat(entity.getPasswordHash()).isEqualTo(HASH);
    assertThat(entity.getFirstName()).isEqualTo("Jane");
    assertThat(entity.getStatus()).isEqualTo(UserStatus.ACTIVE);
  }

  @Test
  void passwordHashNeverExposedInToString() {
    // Given
    User user = User.builder()
        .id(UserId.of(userUuid))
        .tenantId(TenantId.of(tenantUuid))
        .username(Username.of(USERNAME))
        .email(EmailAddress.of(EMAIL))
        .passwordHash(PasswordHash.of(HASH))
        .status(UserStatus.ACTIVE)
        .build();

    // When / Then
    assertThat(user.toString()).doesNotContain(HASH);
  }
}

