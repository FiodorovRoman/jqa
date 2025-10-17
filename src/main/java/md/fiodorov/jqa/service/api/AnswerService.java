package md.fiodorov.jqa.service.api;

import java.util.List;
import md.fiodorov.jqa.domain.Answer;

public interface AnswerService {

  List<Answer> getAnswersForQuestion(Long questionId);

  Answer getAnswerById(Long id);

  Answer addAnswer(Long questionId, Answer answer);

  Answer updateAnswer(Answer answer);

  void deleteAnswer(Long id);

  int upvoteAnswer(Long id);

  int downvoteAnswer(Long id);
}
