package md.fiodorov.jqa.repository;

import java.util.List;
import java.util.Optional;
import md.fiodorov.jqa.domain.Answer;

public interface AnswerRepository {

  Optional<Answer> findById(Long id);

  List<Answer> findByQuestionId(Long questionId);

  Optional<Answer> findRightByQuestionId(Long questionId);

  Answer save(Answer answer);

  void deleteById(Long id);

  void unsetRightForQuestion(Long questionId);
}
