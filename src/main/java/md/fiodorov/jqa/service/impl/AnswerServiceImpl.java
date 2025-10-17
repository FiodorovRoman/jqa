package md.fiodorov.jqa.service.impl;

import java.util.List;
import java.util.Objects;
import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.repository.AnswerRepository;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.service.api.AnswerService;

public class AnswerServiceImpl implements AnswerService {

  private final AnswerRepository answerRepository;
  private final QuestionRepository questionRepository;

  public AnswerServiceImpl(AnswerRepository answerRepository, QuestionRepository questionRepository) {
    this.answerRepository = answerRepository;
    this.questionRepository = questionRepository;
  }

  @Override
  public List<Answer> getAnswersForQuestion(Long questionId) {
    Objects.requireNonNull(questionId, "Question id is required");
    ensureQuestionExists(questionId);
    return answerRepository.findByQuestionId(questionId);
  }

  @Override
  public Answer getAnswerById(Long id) {
    return answerRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + id));
  }

  @Override
  public Answer addAnswer(Long questionId, Answer answer) {
    Objects.requireNonNull(answer, "Answer is required");
    Objects.requireNonNull(questionId, "Question id is required");
    ensureQuestionExists(questionId);
    answer.setQuestionId(questionId);
    return answerRepository.save(answer);
  }

  @Override
  public Answer updateAnswer(Answer answer) {
    Long id = Objects.requireNonNull(answer.getId(), "Answer id is required for update");
    Answer existing =
        answerRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + id));
    existing.setContent(answer.getContent());
    existing.setCreatedBy(answer.getCreatedBy());
    existing.setCreatedAt(answer.getCreatedAt());
    existing.setEditedAt(answer.getEditedAt());
    existing.setEditedBy(answer.getEditedBy());
    return answerRepository.save(existing);
  }

  @Override
  public void deleteAnswer(Long id) {
    answerRepository.deleteById(id);
  }

  @Override
  public int upvoteAnswer(Long id) {
    return changeVote(id, 1);
  }

  @Override
  public int downvoteAnswer(Long id) {
    return changeVote(id, -1);
  }

  private int changeVote(Long id, int delta) {
    Answer answer =
        answerRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + id));
    answer.setVotes(answer.getVotes() + delta);
    answerRepository.save(answer);
    return answer.getVotes();
  }

  private void ensureQuestionExists(Long questionId) {
    questionRepository
        .findById(questionId)
        .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
  }
}
