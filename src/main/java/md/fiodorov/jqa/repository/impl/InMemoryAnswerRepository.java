package md.fiodorov.jqa.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.repository.AnswerRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAnswerRepository implements AnswerRepository {

  private final Map<Long, Answer> storage = new ConcurrentHashMap<>();
  private final AtomicLong seq = new AtomicLong(0);

  @Override
  public Optional<Answer> findById(Long id) {
    return Optional.ofNullable(storage.get(id));
  }

  @Override
  public List<Answer> findByQuestionId(Long questionId) {
    return storage.values().stream()
        .filter(a -> questionId != null && questionId.equals(a.getQuestionId()))
        .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public Optional<Answer> findRightByQuestionId(Long questionId) {
    return storage.values().stream()
        .filter(a -> questionId != null && questionId.equals(a.getQuestionId()) && a.isRight())
        .findFirst();
  }

  @Override
  public Answer save(Answer answer) {
    if (answer.getId() == null) {
      answer.setId(seq.incrementAndGet());
    }
    storage.put(answer.getId(), answer);
    return answer;
  }

  @Override
  public void deleteById(Long id) {
    storage.remove(id);
  }

  @Override
  public void unsetRightForQuestion(Long questionId) {
    storage.values().forEach(a -> {
      if (questionId != null && questionId.equals(a.getQuestionId())) {
        a.setRight(false);
      }
    });
  }
}
