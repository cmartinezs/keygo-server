package io.cmartinezs.keygo.supabase.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    TenantEntity tenantEntity = TenantEntity.builder().id(tenantUuid).build();
    PlatformUserEntity platformUserEntity =
        PlatformUserEntity.builder()
            .id(UUID.randomUUID())
            .email(EMAIL)
            .passwordHash(HASH)
            .firstName("John")
            .lastName("Doe")
            .build();
    TenantUserEntity entity =
        TenantUserEntity.builder()
            .id(userUuid)
            .tenant(tenantEntity)
            .localUsername(USERNAME)
            .platformUser(platformUserEntity)
            .status(UserStatus.ACTIVE)
            .build();

    User user = mapper.toDomain(entity);

    assertThat(user.getId().value()).isEqualTo(userUuid);
    assertThat(user.getTenantId().value()).isEqualTo(tenantUuid);
    assertThat(user.getUsername().value()).isEqualTo(USERNAME);
    assertThat(user.getEmail().value()).isEqualTo(EMAIL);
    assertThat(user.getPasswordHash().value()).isEqualTo(HASH);
    assertThat(user.getFirstName()).isEqualTo("John");
    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
  }

  @Test
  void passwordHashNeverExposedInToString() {
    User user =
        User.builder()
            .id(UserId.of(userUuid))
            .tenantId(TenantId.of(tenantUuid))
            .username(Username.of(USERNAME))
            .email(EmailAddress.of(EMAIL))
            .passwordHash(PasswordHash.of(HASH))
            .status(UserStatus.ACTIVE)
            .build();

    assertThat(user.toString()).doesNotContain(HASH);
  }
}
