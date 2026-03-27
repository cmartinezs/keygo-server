package io.cmartinezs.keygo.app.tenant.usecase;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.filter.TenantFilter;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListTenantsUseCase")
class ListTenantsUseCaseTest {

  @Mock
  private TenantRepositoryPort tenantRepositoryPort;

  @InjectMocks
  private ListTenantsUseCase useCase;

  private Tenant buildTenant(String slug, TenantStatus status) {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(slug))
        .name("Tenant " + slug)
        .ownerEmail("owner@" + slug + ".com")
        .status(status)
        .build();
  }

  @Test
  @DisplayName("should return paginated list when repository has tenants")
  void shouldReturnPaginatedList() {
    // Given
    TenantFilter filter = TenantFilter.of(null, null, 0, 20);
    List<Tenant> tenants = List.of(
        buildTenant("alpha", TenantStatus.ACTIVE),
        buildTenant("beta", TenantStatus.SUSPENDED)
    );
    PagedResult<Tenant> expected = PagedResult.of(tenants, 0, 20, 2L);
    when(tenantRepositoryPort.findAll(filter)).thenReturn(expected);

    // When
    PagedResult<Tenant> result = useCase.execute(filter);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2L);
    assertThat(result.getPage()).isZero();
    assertThat(result.getTotalPages()).isEqualTo(1);
    verify(tenantRepositoryPort).findAll(filter);
  }

  @Test
  @DisplayName("should return empty result when no tenants match filter")
  void shouldReturnEmptyResultWhenNoMatch() {
    // Given
    TenantFilter filter = TenantFilter.of(TenantStatus.SUSPENDED, "nonexistent", 0, 20);
    PagedResult<Tenant> expected = PagedResult.of(List.of(), 0, 20, 0L);
    when(tenantRepositoryPort.findAll(filter)).thenReturn(expected);

    // When
    PagedResult<Tenant> result = useCase.execute(filter);

    // Then
    assertThat(result.isEmpty()).isTrue();
    assertThat(result.getTotalElements()).isZero();
    assertThat(result.getTotalPages()).isZero();
  }

  @Test
  @DisplayName("should pass filter to repository unchanged")
  void shouldDelegateFilterToRepository() {
    // Given
    TenantFilter filter = TenantFilter.of(TenantStatus.ACTIVE, "acme", 1, 10);
    when(tenantRepositoryPort.findAll(any())).thenReturn(PagedResult.of(List.of(), 1, 10, 0L));

    // When
    useCase.execute(filter);

    // Then
    verify(tenantRepositoryPort).findAll(filter);
    verifyNoMoreInteractions(tenantRepositoryPort);
  }

  @Test
  @DisplayName("should compute isLast correctly for last page")
  void shouldComputeIsLastForLastPage() {
    // Given
    TenantFilter filter = TenantFilter.of(null, null, 0, 5);
    List<Tenant> tenants = List.of(buildTenant("only", TenantStatus.ACTIVE));
    PagedResult<Tenant> singlePage = PagedResult.of(tenants, 0, 5, 1L);
    when(tenantRepositoryPort.findAll(filter)).thenReturn(singlePage);

    // When
    PagedResult<Tenant> result = useCase.execute(filter);

    // Then
    assertThat(result.isLast()).isTrue();
    assertThat(result.getTotalPages()).isEqualTo(1);
  }
}

