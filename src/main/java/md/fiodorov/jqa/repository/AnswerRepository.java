package md.fiodorov.jqa.repository;

import java.util.List;
import java.util.Optional;
import md.fiodorov.jqa.domain.Answer;

public interface AnswerRepository {

  List<Answer> findByQuestionId(Long questionId);

  Optional<Answer> findById(Long id);

  Answer save(Answer answer);

  void deleteById(Long id);
}
