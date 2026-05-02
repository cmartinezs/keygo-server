package io.cmartinezs.keygo.api.clientapp.controller;

import io.cmartinezs.keygo.api.clientapp.request.CreateClientAppRequest;
import io.cmartinezs.keygo.api.clientapp.request.UpdateClientAppRequest;
import io.cmartinezs.keygo.app.clientapp.filter.ClientAppFilter;
import io.cmartinezs.keygo.app.clientapp.usecase.CreateClientAppResult;
import io.cmartinezs.keygo.app.clientapp.usecase.CreateClientAppUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.GetClientAppUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.ListClientAppsUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.RotateClientSecretUseCase;
import io.cmartinezs.keygo.app.clientapp.usecase.RotateSecretResult;
import io.cmartinezs.keygo.app.clientapp.usecase.UpdateClientAppUseCase;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantClientAppControllerTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID = "app-abc";

  @Mock private CreateClientAppUseCase createClientAppUseCase;
  @Mock private ListClientAppsUseCase listClientAppsUseCase;
  @Mock private GetClientAppUseCase getClientAppUseCase;
  @Mock private UpdateClientAppUseCase updateClientAppUseCase;
  @Mock private RotateClientSecretUseCase rotateClientSecretUseCase;

  @InjectMocks
  private TenantClientAppController controller;

  private ClientApp confidentialApp() {
    return ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(TenantId.generate())
        .clientId(ClientId.of(CLIENT_ID))
        .name("My App")
        .type(ClientType.CONFIDENTIAL)
        .hashedSecret("$2a$hashed")
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  private ClientApp publicApp() {
    return ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(TenantId.generate())
        .clientId(ClientId.of(CLIENT_ID))
        .name("My App")
        .type(ClientType.PUBLIC)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  @Test
  void createClientApp_confidential_shouldReturn201WithSecret() {
    // Given
    CreateClientAppRequest request = new CreateClientAppRequest(
        "My App", null, ClientType.CONFIDENTIAL,
        Set.of("https://example.com/callback"),
        Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of());
    when(createClientAppUseCase.execute(any()))
        .thenReturn(new CreateClientAppResult(confidentialApp(), "rawSecret"));

    // When
    var response = controller.createClientApp(TENANT_SLUG, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().getClientId()).isEqualTo(CLIENT_ID);
    assertThat(response.getBody().getData().getClientSecret()).isEqualTo("rawSecret");
  }

  @Test
  void createClientApp_public_shouldReturn201WithNullSecret() {
    // Given
    CreateClientAppRequest request = new CreateClientAppRequest(
        "My App", null, ClientType.PUBLIC,
        Set.of("https://example.com/callback"),
        Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of());
    when(createClientAppUseCase.execute(any()))
        .thenReturn(new CreateClientAppResult(publicApp(), null));

    // When
    var response = controller.createClientApp(TENANT_SLUG, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().getData().getClientSecret()).isNull();
  }

  @Test
  void listClientApps_shouldReturn200WithList() {
    // Given
    PagedResult<ClientApp> pagedResult = PagedResult.of(List.of(publicApp(), publicApp()), 0, 20, 2);
    when(listClientAppsUseCase.execute(eq(TENANT_SLUG), any(ClientAppFilter.class)))
        .thenReturn(pagedResult);

    // When
    var response = controller.listClientApps(TENANT_SLUG, null, null, 0, 20, null, null);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getContent()).hasSize(2);
  }

  @Test
  void getClientApp_existing_shouldReturn200() {
    // Given
    when(getClientAppUseCase.execute(TENANT_SLUG, CLIENT_ID)).thenReturn(publicApp());

    // When
    var response = controller.getClientApp(TENANT_SLUG, CLIENT_ID);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getClientId()).isEqualTo(CLIENT_ID);
  }

  @Test
  void getClientApp_notFound_shouldPropagate() {
    // Given
    when(getClientAppUseCase.execute(TENANT_SLUG, "missing"))
        .thenThrow(new ClientAppNotFoundException("missing"));

    // When / Then
    assertThatThrownBy(() -> controller.getClientApp(TENANT_SLUG, "missing"))
        .isInstanceOf(ClientAppNotFoundException.class);
  }

  @Test
  void updateClientApp_shouldReturn200() {
    // Given
    UpdateClientAppRequest request = new UpdateClientAppRequest(
        "Updated Name", null, Set.of("https://example.com/callback"),
        Set.of(AllowedGrant.CLIENT_CREDENTIALS), Set.of());
    when(updateClientAppUseCase.execute(any())).thenReturn(publicApp());

    // When
    var response = controller.updateClientApp(TENANT_SLUG, CLIENT_ID, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void rotateClientSecret_shouldReturn200WithNewSecret() {
    // Given
    when(rotateClientSecretUseCase.execute(TENANT_SLUG, CLIENT_ID))
        .thenReturn(new RotateSecretResult(confidentialApp(), "newRawSecret"));

    // When
    var response = controller.rotateClientSecret(TENANT_SLUG, CLIENT_ID);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getClientSecret()).isEqualTo("newRawSecret");
  }
}

