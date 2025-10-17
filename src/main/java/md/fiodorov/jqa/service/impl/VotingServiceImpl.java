package md.fiodorov.jqa.service.impl;

import java.util.Objects;
import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.domain.Question;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.AnswerRepository;
import md.fiodorov.jqa.repository.ConfigRepository;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.repository.UserRepository;
import md.fiodorov.jqa.service.api.VotingService;
import org.springframework.stereotype.Service;

@Service
public class VotingServiceImpl implements VotingService {

  private final QuestionRepository questionRepository;
  private final AnswerRepository answerRepository;
  private final UserRepository userRepository;
  private final ConfigRepository configRepository;

  public VotingServiceImpl(QuestionRepository questionRepository,
                           AnswerRepository answerRepository,
                           UserRepository userRepository,
                           ConfigRepository configRepository) {
    this.questionRepository = Objects.requireNonNull(questionRepository);
    this.answerRepository = Objects.requireNonNull(answerRepository);
    this.userRepository = Objects.requireNonNull(userRepository);
    this.configRepository = Objects.requireNonNull(configRepository);
  }

  @Override
  public int voteQuestionUp(Long questionId) {
    return applyQuestionVote(questionId, true);
  }

  @Override
  public int voteQuestionDown(Long questionId) {
    return applyQuestionVote(questionId, false);
  }

  @Override
  public int voteAnswerUp(Long answerId) {
    return applyAnswerVote(answerId, true);
  }

  @Override
  public int voteAnswerDown(Long answerId) {
    return applyAnswerVote(answerId, false);
  }

  private int applyQuestionVote(Long questionId, boolean up) {
    Objects.requireNonNull(questionId, "questionId");
    Question q = questionRepository.findById(questionId)
        .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
    int delta = up
        ? Math.max(0, configRepository.getInt("points.question.upvote", 10))
        : -Math.max(0, configRepository.getInt("points.question.downvote", 2));
    q.setRank(q.getRank() + (up ? 1 : -1));
    questionRepository.save(q);
    User author = q.getCreatedBy();
    if (author != null) {
      author.setRating(author.getRating() + delta);
      userRepository.save(author);
    }
    return q.getRank();
  }

  private int applyAnswerVote(Long answerId, boolean up) {
    Objects.requireNonNull(answerId, "answerId");
    Answer a = answerRepository.findById(answerId)
        .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + answerId));
    int delta = up
        ? Math.max(0, configRepository.getInt("points.answer.upvote", 10))
        : -Math.max(0, configRepository.getInt("points.answer.downvote", 2));
    a.setRank(a.getRank() + (up ? 1 : -1));
    answerRepository.save(a);
    User author = a.getCreatedBy();
    if (author != null) {
      author.setRating(author.getRating() + delta);
      userRepository.save(author);
    }
    return a.getRank();
  }

  // helper for AnswerServiceImpl to award accepted answer
  public void awardAcceptedAnswerPoints(Answer answer) {
    if (answer == null) return;
    int points = Math.max(0, configRepository.getInt("points.answer.accepted", 15));
    User author = answer.getCreatedBy();
    if (author != null) {
      author.setRating(author.getRating() + points);
      userRepository.save(author);
    }
  }
}
