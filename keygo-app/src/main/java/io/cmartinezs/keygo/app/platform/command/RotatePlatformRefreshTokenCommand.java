package io.cmartinezs.keygo.app.platform.command;

/**
 * Comando para rotar un refresh token de plataforma.
 *
 * @param refreshToken token plano para rotación
 */
public record RotatePlatformRefreshTokenCommand(String refreshToken) {

  public RotatePlatformRefreshTokenCommand {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new IllegalArgumentException("Refresh token cannot be null or blank");
    }
  }
}
