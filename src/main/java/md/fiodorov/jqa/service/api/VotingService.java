package md.fiodorov.jqa.service.api;

public interface VotingService {
  int voteQuestionUp(Long questionId);
  int voteQuestionDown(Long questionId);
  int voteAnswerUp(Long answerId);
  int voteAnswerDown(Long answerId);
}
