package md.fiodorov.jqa.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.domain.Question;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.AnswerRepository;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.service.api.AnswerService;
import org.springframework.stereotype.Service;

@Service
public class AnswerServiceImpl implements AnswerService {

  private final AnswerRepository answerRepository;
  private final QuestionRepository questionRepository;
  // Optional dependencies for awarding points on acceptance
  private final md.fiodorov.jqa.repository.UserRepository userRepository; // may be null in tests
  private final md.fiodorov.jqa.repository.ConfigRepository configRepository; // may be null in tests

  // Primary constructor used by Spring
  @org.springframework.beans.factory.annotation.Autowired
  public AnswerServiceImpl(AnswerRepository answerRepository,
                           QuestionRepository questionRepository,
                           md.fiodorov.jqa.repository.UserRepository userRepository,
                           md.fiodorov.jqa.repository.ConfigRepository configRepository) {
    this.answerRepository = Objects.requireNonNull(answerRepository, "answerRepository");
    this.questionRepository = Objects.requireNonNull(questionRepository, "questionRepository");
    this.userRepository = userRepository;
    this.configRepository = configRepository;
  }

  // Convenience constructor for existing unit tests
  public AnswerServiceImpl(AnswerRepository answerRepository, QuestionRepository questionRepository) {
    this(answerRepository, questionRepository, null, null);
  }

  @Override
  public Answer addAnswer(Long questionId, String content, User author, Instant createdAt) {
    Objects.requireNonNull(questionId, "questionId");
    Objects.requireNonNull(content, "content");
    Objects.requireNonNull(author, "author");

    // Ensure question exists
    Question q = questionRepository.findById(questionId)
        .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));

    Answer a = new Answer();
    a.setQuestionId(q.getId());
    a.setContent(content);
    a.setCreatedBy(author);
    a.setCreatedAt(createdAt != null ? createdAt : Instant.now());
    a.setRight(false);
    return answerRepository.save(a);
  }

  @Override
  public List<Answer> listAnswers(Long questionId) {
    Objects.requireNonNull(questionId, "questionId");
    return answerRepository.findByQuestionId(questionId);
  }

  @Override
  public Answer markAsRight(Long answerId) {
    Objects.requireNonNull(answerId, "answerId");

    Answer answer = answerRepository.findById(answerId)
        .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + answerId));

    Long questionId = answer.getQuestionId();
    Question q = questionRepository.findById(questionId)
        .orElseThrow(() -> new IllegalStateException("Question not found for answer: " + questionId));

    // Unset any previous right answers for this question
    answerRepository.unsetRightForQuestion(questionId);

    // Set selected answer as right and save
    answer.setRight(true);
    Answer saved = answerRepository.save(answer);

    // Close the question
    if (!q.isClosed()) {
      q.setClosed(true);
      questionRepository.save(q);
    }

    // Award points to the author of the accepted answer if config/user repositories are available
    if (userRepository != null && configRepository != null) {
      int points = Math.max(0, configRepository.getInt("points.answer.accepted", 15));
      User author = saved.getCreatedBy();
      if (author != null) {
        author.setRating(author.getRating() + points);
        userRepository.save(author);
      }
    }

    return saved;
  }

  @Override
  public void deleteAnswerById(Long answerId) {
    Objects.requireNonNull(answerId, "answerId");
    answerRepository.deleteById(answerId);
  }
}
