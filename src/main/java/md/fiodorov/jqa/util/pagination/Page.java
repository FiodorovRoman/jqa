package md.fiodorov.jqa.util.pagination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class Page<T> {

  private final List<T> content;
  private final Pageable pageable;

  public Page(List<T> content, Pageable pageable) {
    this.content = Collections.unmodifiableList(new ArrayList<>(content));
    this.pageable = Objects.requireNonNull(pageable, "pageable");
  }

  public List<T> getContent() {
    return content;
  }

  public Pageable getPageable() {
    return pageable;
  }

  public <R> Page<R> map(Function<? super T, ? extends R> mapper) {
    List<R> mapped = new ArrayList<>(content.size());
    for (T element : content) {
      mapped.add(mapper.apply(element));
    }
    return new Page<>(mapped, pageable);
  }
}
