package md.fiodorov.jqa.repository.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import md.fiodorov.jqa.domain.Question;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.util.pagination.Page;
import md.fiodorov.jqa.util.pagination.PageRequest;
import md.fiodorov.jqa.util.pagination.Pageable;
import md.fiodorov.jqa.util.pagination.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryQuestionRepository implements QuestionRepository {

  private final Map<Long, Question> storage = new ConcurrentHashMap<>();
  private final AtomicLong seq = new AtomicLong(0);

  @Override
  public Page<Question> findAll(Pageable pageable) {
    List<Question> all = new ArrayList<>(storage.values());

    // Sorting (support by("createdAt") and by("title") at minimum)
    Sort sort = pageable.getSort();
    Comparator<Question> comparator = Comparator.comparing(Question::getId); // default
    if (sort != null) {
      String property = sort.getProperty();
      Comparator<Question> c;
      if ("createdAt".equals(property)) {
        c = Comparator.comparing(q -> Optional.ofNullable(q.getCreatedAt()).orElse(Instant.EPOCH));
      } else if ("title".equals(property)) {
        c = Comparator.comparing(q -> Optional.ofNullable(q.getTitle()).orElse(""));
      } else {
        c = Comparator.comparing(Question::getId);
      }
      comparator = sort.getDirection() == Sort.Direction.DESC ? c.reversed() : c;
    }
    all.sort(comparator);

    int page = pageable.getPageNumber();
    int size = pageable.getPageSize();
    int from = Math.min(page * size, all.size());
    int to = Math.min(from + size, all.size());
    List<Question> content = all.subList(from, to);

    return new Page<>(content, PageRequest.of(page, size, sort));
  }

  @Override
  public Optional<Question> findById(Long id) {
    return Optional.ofNullable(storage.get(id));
  }

  @Override
  public Question save(Question question) {
    if (question.getId() == null) {
      question.setId(seq.incrementAndGet());
    }
    storage.put(question.getId(), question);
    return question;
  }

  @Override
  public void deleteById(Long id) {
    storage.remove(id);
  }
}
