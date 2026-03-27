package io.cmartinezs.keygo.api.shared.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Generic paginated response DTO for REST API responses.
 * <p>DTO de respuesta paginada genérica para respuestas de la API REST.
 *
 * @param <T> the type of data items / tipo de ítems de datos
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class PagedData<T> {

  /** The items on the current page. / Los ítems de la página actual. */
  private final List<T> content;

  /** Zero-based current page number. / Número de página actual (base 0). */
  private final int page;

  /** Page size. / Tamaño de página. */
  private final int size;

  /** Total number of elements across all pages. / Total de elementos en todas las páginas. */
  private final long totalElements;

  /** Total number of pages. / Total de páginas. */
  private final int totalPages;

  /** True if this is the last page. / True si es la última página. */
  private final boolean last;
}

