package io.cmartinezs.keygo.app.auth.port;

/**
 * Port OUT — unified contract for encoding and verifying credentials (passwords, secrets).
 * <p>Puerto de salida unificado — contrato para codificar y verificar credenciales
 * (contraseñas de usuario, secrets de cliente OAuth2).
 * @author cmartinezs
 * @version 1.0
 */
public interface CredentialEncoderPort {

  /**
   * Encode a raw credential into a hashed representation.
   * <p>Codifica una credencial en texto plano a su representación hasheada.
   * @param rawCredential the raw credential to encode (password or secret)
   * @return the encoded credential
   */
  String encode(String rawCredential);

  /**
   * Check whether a raw credential matches an encoded one.
   * <p>Verifica si una credencial en texto plano coincide con su versión codificada.
   * @param rawCredential the raw credential
   * @param encodedCredential the encoded credential to compare against
   * @return true if they match
   */
  boolean matches(String rawCredential, String encodedCredential);
}

