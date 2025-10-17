package md.fiodorov.jqa.repository;

import java.util.Optional;
import md.fiodorov.jqa.domain.Question;
import md.fiodorov.jqa.util.pagination.Page;
import md.fiodorov.jqa.util.pagination.Pageable;

public interface QuestionRepository {

  Page<Question> findAll(Pageable pageable);

  Optional<Question> findById(Long id);

  Question save(Question question);

  void deleteById(Long id);
}
