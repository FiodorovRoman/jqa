package md.fiodorov.jqa.repository;

import java.util.List;
import java.util.Optional;
import md.fiodorov.jqa.domain.Comment;

public interface CommentRepository {

  List<Comment> findByQuestionId(Long questionId);

  List<Comment> findByAnswerId(Long answerId);

  Optional<Comment> findById(Long id);

  Comment save(Comment comment);

  void deleteById(Long id);
}
