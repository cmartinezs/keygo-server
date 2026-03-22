package io.cmartinezs.keygo.run.config.auth;

import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyAlgorithm;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyId;
import io.cmartinezs.keygo.domain.auth.model.SigningKeyStatus;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Servicio de bootstrap de clave de firma RSA.
 *
 * <p>Se ejecuta al arrancar la aplicación (solo con perfil {@code supabase}). Si no existe
 * ninguna clave ACTIVE en la base de datos, genera automáticamente un par de claves RSA 2048-bit
 * y lo persiste. Esto garantiza que el servidor siempre pueda emitir tokens JWT.
 *
 * <p><b>ADVERTENCIA:</b> En producción, la clave privada debería gestionarse mediante un KMS
 * externo (AWS KMS, Azure Key Vault, HashiCorp Vault). La generación automática es solo para
 * entornos de desarrollo y staging.
 */
@Slf4j
@Component
@Profile("supabase")
@RequiredArgsConstructor
public class SigningKeyBootstrapService {

  private final SigningKeyRepositoryPort signingKeyRepositoryPort;

  @EventListener(ApplicationReadyEvent.class)
  public void bootstrapSigningKey() {
    if (signingKeyRepositoryPort.findActiveKey().isPresent()) {
      log.info("[SigningKeyBootstrap] Active signing key found — skipping generation.");
      return;
    }

    log.warn("[SigningKeyBootstrap] No active signing key found — generating RSA 2048-bit key pair.");
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      KeyPair keyPair = generator.generateKeyPair();

      String publicPem = toPem("PUBLIC KEY", keyPair.getPublic().getEncoded());
      String privatePem = toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded());

      SigningKey newKey = SigningKey.builder()
          .id(new SigningKeyId(UUID.randomUUID().toString()))
          .kid(UUID.randomUUID().toString())
          .algorithm(SigningKeyAlgorithm.RS256)
          .status(SigningKeyStatus.ACTIVE)
          .publicMaterial(publicPem)
          .privateMaterial(privatePem)
          .activatedAt(Instant.now())
          .build();

      signingKeyRepositoryPort.save(newKey);
      log.info("[SigningKeyBootstrap] RSA key pair generated and persisted (kid={}).", newKey.getKid());

    } catch (Exception e) {
      log.error("[SigningKeyBootstrap] Failed to generate signing key: {}", e.getMessage(), e);
      throw new IllegalStateException("Cannot start application: failed to initialize signing key", e);
    }
  }

  private static String toPem(String label, byte[] encoded) {
    String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded);
    return "-----BEGIN " + label + "-----\n" + base64 + "\n-----END " + label + "-----\n";
  }
}

