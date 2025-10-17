package md.fiodorov.jqa.util.pagination;

import java.util.Objects;

public final class PageRequest implements Pageable {

  private final int pageNumber;
  private final int pageSize;
  private final Sort sort;

  private PageRequest(int pageNumber, int pageSize, Sort sort) {
    if (pageNumber < 0) {
      throw new IllegalArgumentException("Page number must be non-negative");
    }
    if (pageSize <= 0) {
      throw new IllegalArgumentException("Page size must be greater than zero");
    }
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.sort = Objects.requireNonNull(sort, "sort");
  }

  public static PageRequest of(int pageNumber, int pageSize) {
    return new PageRequest(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));
  }

  public static PageRequest of(int pageNumber, int pageSize, Sort sort) {
    return new PageRequest(pageNumber, pageSize, sort);
  }

  @Override
  public int getPageNumber() {
    return pageNumber;
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  @Override
  public Sort getSort() {
    return sort;
  }
}
