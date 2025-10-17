package md.fiodorov.jqa.controller;

import java.util.List;
import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.service.api.AnswerService;

public class AnswerController {

  private final AnswerService answerService;

  public AnswerController(AnswerService answerService) {
    this.answerService = answerService;
  }

  public List<Answer> listAnswers(Long questionId) {
    return answerService.getAnswersForQuestion(questionId);
  }

  public Answer getAnswer(Long id) {
    return answerService.getAnswerById(id);
  }

  public Answer createAnswer(Long questionId, Answer answer) {
    return answerService.addAnswer(questionId, answer);
  }

  public Answer updateAnswer(Answer answer) {
    return answerService.updateAnswer(answer);
  }

  public void deleteAnswer(Long id) {
    answerService.deleteAnswer(id);
  }

  public int upvoteAnswer(Long id) {
    return answerService.upvoteAnswer(id);
  }

  public int downvoteAnswer(Long id) {
    return answerService.downvoteAnswer(id);
  }
}
