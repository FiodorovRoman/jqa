package md.fiodorov.jqa.service.api;

import java.time.Instant;
import java.util.List;
import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.domain.User;

public interface AnswerService {

  Answer addAnswer(Long questionId, String content, User author, Instant createdAt);

  List<Answer> listAnswers(Long questionId);

  Answer markAsRight(Long answerId);

  void deleteAnswerById(Long answerId);
}
