package io.cmartinezs.keygo.app.usecase;

import io.cmartinezs.keygo.app.port.out.ServiceInfoProvider;

/**
 * Use case for retrieving service public information
 * Caso de uso para obtener información pública del servicio
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetServiceInfoUseCase {

  private final ServiceInfoProvider serviceInfoProvider;

  public GetServiceInfoUseCase(ServiceInfoProvider serviceInfoProvider) {
    this.serviceInfoProvider = serviceInfoProvider;
  }

  /**
   * Execute use case to get service information
   * @return ServiceInfoProvider with service information
   */
  public ServiceInfoProvider execute() {
    return serviceInfoProvider;
  }
}

