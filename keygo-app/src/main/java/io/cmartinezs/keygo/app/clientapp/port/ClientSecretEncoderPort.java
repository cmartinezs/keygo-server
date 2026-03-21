package io.cmartinezs.keygo.app.clientapp.port;

/**
 * Port OUT — contract for encoding and verifying client secrets.
 * <p>Puerto de salida — contrato para codificar y verificar secrets de clientes.
 * @author cmartinezs
 * @version 1.0
 */
public interface ClientSecretEncoderPort {

  /**
   * Encode a raw client secret into a hashed representation.
   * <p>Codifica un secret en texto plano a su representación hasheada.
   * @param rawSecret the raw secret to encode
   * @return the encoded secret
   */
  String encode(String rawSecret);

  /**
   * Check whether a raw secret matches an encoded one.
   * <p>Verifica si un secret en texto plano coincide con su versión codificada.
   * @param rawSecret the raw secret
   * @param encodedSecret the encoded secret to compare against
   * @return true if they match
   */
  boolean matches(String rawSecret, String encodedSecret);
}

