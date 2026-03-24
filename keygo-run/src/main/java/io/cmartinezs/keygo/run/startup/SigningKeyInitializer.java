package io.cmartinezs.keygo.run.startup;

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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Startup initializer that ensures at least one active RSA signing key exists.
 *
 * <p>Active only when the {@code supabase} profile is loaded. On first startup (empty DB),
 * generates an RSA-2048 key pair, formats it as PEM, and persists it via
 * {@link SigningKeyRepositoryPort}. On subsequent startups, this is a no-op.
 *
 * <p>The generated key:
 * <ul>
 *   <li>Algorithm: RS256</li>
 *   <li>Key size: 2048 bits</li>
 *   <li>Status: ACTIVE</li>
 *   <li>kid: a random UUID string</li>
 * </ul>
 */
@Slf4j
@Component
@Profile("supabase")
@RequiredArgsConstructor
public class SigningKeyInitializer implements ApplicationRunner {

  private final SigningKeyRepositoryPort signingKeyRepository;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (signingKeyRepository.findActiveKey().isPresent()) {
      log.info("SigningKeyInitializer: active signing key already exists — skipping generation");
      return;
    }

    log.info("SigningKeyInitializer: no active signing key found — generating RSA-2048 key pair...");

    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair pair = generator.generateKeyPair();

    String publicPem = toPem("PUBLIC KEY", pair.getPublic().getEncoded());
    String privatePem = toPem("PRIVATE KEY", pair.getPrivate().getEncoded());

    String kid = UUID.randomUUID().toString();
    Instant now = Instant.now();

    SigningKey key = SigningKey.builder()
        .id(new SigningKeyId(UUID.randomUUID().toString()))
        .kid(kid)
        .algorithm(SigningKeyAlgorithm.RS256)
        .status(SigningKeyStatus.ACTIVE)
        .publicMaterial(publicPem)
        .privateMaterial(privatePem)
        .activatedAt(now)
        .build();

    signingKeyRepository.save(key);
    log.info("SigningKeyInitializer: RSA-2048 signing key generated and persisted (kid={})", kid);
  }

  /**
   * Encodes raw key bytes into a standard PEM block.
   *
   * @param label  PEM label (e.g. {@code "PUBLIC KEY"} or {@code "PRIVATE KEY"})
   * @param bytes  DER-encoded key bytes
   * @return PEM string including headers
   */
  private static String toPem(String label, byte[] bytes) {
    String encoded = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(bytes);
    return "-----BEGIN " + label + "-----\n" + encoded + "\n-----END " + label + "-----";
  }
}

