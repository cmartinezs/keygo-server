package io.cmartinezs.keygo.app.clientapp.port;

/**
 * Port OUT — contract for generating OAuth2 client credentials.
 * <p>Puerto de salida — contrato para generar credenciales OAuth2 de cliente.
 * @author cmartinezs
 * @version 1.0
 */
public interface ClientCredentialGeneratorPort {

  /**
   * Generate a new unique client_id.
   * <p>Genera un nuevo client_id único.
   * @return the generated client_id string
   */
  String generateClientId();

  /**
   * Generate a new cryptographically secure client secret.
   * <p>Genera un nuevo secret de cliente criptográficamente seguro.
   * @return the generated raw client secret
   */
  String generateClientSecret();
}

