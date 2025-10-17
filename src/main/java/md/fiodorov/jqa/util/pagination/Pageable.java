package md.fiodorov.jqa.util.pagination;

public interface Pageable {

  int getPageNumber();

  int getPageSize();

  Sort getSort();
}
