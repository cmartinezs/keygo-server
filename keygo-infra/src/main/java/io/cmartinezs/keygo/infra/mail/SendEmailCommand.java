package io.cmartinezs.keygo.infra.mail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Comando para envío de email con template Thymeleaf.
 *
 * <p>Propiedades: - emailType: tipo de email ("email-validation", "password-recovery", etc.) -
 * recipientEmail: email del destinatario - recipientName: nombre del destinatario (opcional) -
 * variables: Map de variables que se renderizan en el template - locale: idioma para i18n (default:
 * es_ES)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SendEmailCommand {

  /** Tipo de email: determina qué Strategy + Template usar */
  @NotBlank(message = "Email type es requerido")
  private String emailType;

  /** Email del destinatario */
  @Email(message = "Email válido requerido")
  private String recipientEmail;

  /** Nombre del destinatario (opcional) */
  private String recipientName;

  /** Variables que se pasan al template Thymeleaf */
  @Builder.Default private Map<String, Object> variables = new HashMap<>();

  /** Variables para construir URLs con KeyGoUiProperties (opcional) */
  @Builder.Default private Map<String, Object> pathVariables = new HashMap<>();

  /** Variables para query params en URLs (opcional) */
  @Builder.Default private Map<String, Object> queryParams = new HashMap<>();

  /**
   * Agrega una variable a la lista de variables.
   *
   * @param key nombre de la variable
   * @param value valor de la variable
   * @return this (para chaining)
   */
  public SendEmailCommand withVariable(String key, String value) {
    this.variables.put(key, value);
    return this;
  }

  /**
   * Agrega una variable de path para construcción de URLs con KeyGoUiProperties.
   *
   * @param key nombre del path
   * @param value valor del path
   * @return this (para chaining)
   */
  public SendEmailCommand withPathVariable(String key, String value) {
    this.pathVariables.put(key, value);
    return this;
  }

  /**
   * Agrega una variable de query param para construcción de URLs con KeyGoUiProperties.
   *
   * @param key nombre de la query param
   * @param value valor de la query param
   * @return this (para chaining)
   */
  public SendEmailCommand withQueryParam(String key, String value) {
    this.queryParams.put(key, value);
    return this;
  }
}
