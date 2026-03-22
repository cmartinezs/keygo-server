package io.cmartinezs.keygo.api.auth.controller;

import io.cmartinezs.keygo.app.auth.usecase.GetJwksUseCase;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller: expone el JWKS endpoint (RFC 7517).
 *
 * <p>Retorna JSON nativo (no {@code BaseResponse}) para garantizar interoperabilidad con
 * librerías OAuth2 de terceros que consumen este endpoint directamente.
 *
 * <p>Ruta pública: no requiere {@code X-KEYGO-ADMIN}.
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}")
public class JwksController {

  private final GetJwksUseCase getJwksUseCase;

  public JwksController(GetJwksUseCase getJwksUseCase) {
    this.getJwksUseCase = getJwksUseCase;
  }

  /**
   * GET /api/v1/tenants/{tenantSlug}/.well-known/jwks.json
   *
   * <p>Retorna el JWK Set con las claves públicas RSA activas y retiradas.
   *
   * @param tenantSlug slug del tenant
   * @return JSON con estructura {@code {"keys": [...]}}
   */
  @GetMapping("/.well-known/jwks.json")
  public ResponseEntity<Map<String, Object>> getJwks(@PathVariable String tenantSlug) {
    return ResponseEntity.ok(getJwksUseCase.execute());
  }
}
