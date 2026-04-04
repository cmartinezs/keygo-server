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
import lombok.Setter;

/**
 * Comando para envío de email con template Thymeleaf.
 *
 * <p>Propiedades:
 * - emailType: tipo de email ("email-validation", "password-recovery", etc.)
 * - recipientEmail: email del destinatario
 * - recipientName: nombre del destinatario (opcional)
 * - variables: Map de variables que se renderizan en el template
 * - locale: idioma para i18n (default: es_ES)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
  @Builder.Default
  private Map<String, Object> variables = new HashMap<>();

  /** Locale para i18n (default: español) */
  @Builder.Default
  private Locale locale = Locale.of("es");

  /**
   * Agrega una variable a la lista de variables.
   *
   * @param key nombre de la variable
   * @param value valor de la variable
   * @return this (para chaining)
   */
  public SendEmailCommand withVariable(String key, Object value) {
    this.variables.put(key, value);
    return this;
  }
}

