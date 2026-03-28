package io.cmartinezs.keygo.api.platform.response;

import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import lombok.Builder;
import lombok.Getter;


/**
 * DTO for platform-wide statistics data.
 * <p>DTO para datos de estadísticas a nivel plataforma.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class PlatformStatsData {

  private TenantStats tenants;
  private UserStats users;
  private AppStats apps;
  private SigningKeyStats signingKeys;

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<PlatformStatsData> {
  }

  @Getter
  @Builder
  public static class TenantStats {
    private long total;
    private long active;
    private long suspended;
    private long pending;
  }

  @Getter
  @Builder
  public static class UserStats {
    private long total;
    private long active;
    private long pending;
    private long suspended;
  }

  @Getter
  @Builder
  public static class AppStats {
    private long total;
  }

  @Getter
  @Builder
  public static class SigningKeyStats {
    private long active;
  }
}
