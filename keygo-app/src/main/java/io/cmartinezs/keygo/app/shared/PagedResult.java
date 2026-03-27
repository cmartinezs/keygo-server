package io.cmartinezs.keygo.app.shared;

import java.util.List;

/**
 * Generic paginated result — framework-agnostic wrapper for paginated data.
 * <p>Resultado paginado genérico — contenedor de datos paginados sin dependencias de framework.
 *
 * @param <T> the type of elements in this result / tipo de elementos en este resultado
 * @author cmartinezs
 * @version 1.0
 */
public class PagedResult<T> {

  private final List<T> content;
  private final int page;
  private final int size;
  private final long totalElements;
  private final int totalPages;

  private PagedResult(List<T> content, int page, int size, long totalElements) {
    this.content = List.copyOf(content);
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
  }

  /**
   * Create a new PagedResult.
   *
   * @param content       the page content / el contenido de la página
   * @param page          zero-based page number / número de página (base 0)
   * @param size          page size / tamaño de página
   * @param totalElements total number of elements / total de elementos
   * @param <T>           element type / tipo de elemento
   * @return a new PagedResult instance / nueva instancia de PagedResult
   */
  public static <T> PagedResult<T> of(List<T> content, int page, int size, long totalElements) {
    return new PagedResult<>(content, page, size, totalElements);
  }

  /**
   * The content of the current page.
   * <p>El contenido de la página actual.
   */
  public List<T> getContent() {
    return content;
  }

  /**
   * The zero-based page number.
   * <p>El número de página (base 0).
   */
  public int getPage() {
    return page;
  }

  /**
   * The page size.
   * <p>El tamaño de página.
   */
  public int getSize() {
    return size;
  }

  /**
   * Total number of elements across all pages.
   * <p>Total de elementos en todas las páginas.
   */
  public long getTotalElements() {
    return totalElements;
  }

  /**
   * Total number of pages.
   * <p>Total de páginas.
   */
  public int getTotalPages() {
    return totalPages;
  }

  /**
   * Returns true if there are no elements in this result.
   * <p>Retorna true si no hay elementos en este resultado.
   */
  public boolean isEmpty() {
    return content.isEmpty();
  }

  /**
   * Returns true if this is the last page.
   * <p>Retorna true si esta es la última página.
   */
  public boolean isLast() {
    return page >= totalPages - 1;
  }
}

