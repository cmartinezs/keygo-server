package io.cmartinezs.keygo.run.clientapp;

import io.cmartinezs.keygo.app.clientapp.port.ClientCredentialGeneratorPort;

import java.util.UUID;

/**
 * UUID-based implementation of ClientCredentialGeneratorPort.
 * Generates client_id as a compact UUID (no dashes) and client_secret as two UUIDs concatenated.
 * <p>Implementación de ClientCredentialGeneratorPort basada en UUID.
 * Genera client_id como UUID compacto (sin guiones) y client_secret como dos UUIDs concatenados.
 * @author cmartinezs
 * @version 1.0
 */
public class UuidClientCredentialGenerator implements ClientCredentialGeneratorPort {

  @Override
  public String generateClientId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  @Override
  public String generateClientSecret() {
    return UUID.randomUUID().toString().replace("-", "")
        + UUID.randomUUID().toString().replace("-", "");
  }
}

