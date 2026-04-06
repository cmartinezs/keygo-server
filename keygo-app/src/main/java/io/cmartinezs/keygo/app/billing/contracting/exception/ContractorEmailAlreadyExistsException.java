package io.cmartinezs.keygo.app.billing.contracting.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when attempting to create a contract with an email that already belongs to an existing
 * contractor.
 *
 * <p>Esta excepción se lanza cuando se intenta crear un contrato con un email que ya pertenece a un
 * contratante existente.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ContractorEmailAlreadyExistsException extends UseCaseException {

  private final String email;

  public ContractorEmailAlreadyExistsException(String email) {
    super("A contractor with email '%s' already exists".formatted(email));
    this.email = email;
  }

  public String getEmail() {
    return email;
  }
}
