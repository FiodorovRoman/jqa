package md.fiodorov.jqa.service.impl;

import java.util.List;
import java.util.Objects;
import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.domain.Comment;
import md.fiodorov.jqa.repository.AnswerRepository;
import md.fiodorov.jqa.repository.CommentRepository;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.service.api.CommentService;

public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final QuestionRepository questionRepository;
  private final AnswerRepository answerRepository;

  public CommentServiceImpl(
      CommentRepository commentRepository,
      QuestionRepository questionRepository,
      AnswerRepository answerRepository) {
    this.commentRepository = commentRepository;
    this.questionRepository = questionRepository;
    this.answerRepository = answerRepository;
  }

  @Override
  public List<Comment> getCommentsForQuestion(Long questionId) {
    Objects.requireNonNull(questionId, "Question id is required");
    ensureQuestionExists(questionId);
    return commentRepository.findByQuestionId(questionId);
  }

  @Override
  public List<Comment> getCommentsForAnswer(Long answerId) {
    Objects.requireNonNull(answerId, "Answer id is required");
    ensureAnswerExists(answerId);
    return commentRepository.findByAnswerId(answerId);
  }

  @Override
  public Comment getCommentById(Long id) {
    return commentRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + id));
  }

  @Override
  public Comment addCommentToQuestion(Long questionId, Comment comment) {
    Objects.requireNonNull(comment, "Comment is required");
    Objects.requireNonNull(questionId, "Question id is required");
    ensureQuestionExists(questionId);
    comment.setQuestionId(questionId);
    comment.setAnswerId(null);
    return commentRepository.save(comment);
  }

  @Override
  public Comment addCommentToAnswer(Long answerId, Comment comment) {
    Objects.requireNonNull(comment, "Comment is required");
    Objects.requireNonNull(answerId, "Answer id is required");
    Answer answer = ensureAnswerExists(answerId);
    comment.setAnswerId(answerId);
    comment.setQuestionId(answer.getQuestionId());
    return commentRepository.save(comment);
  }

  @Override
  public Comment updateComment(Comment comment) {
    Long id = Objects.requireNonNull(comment.getId(), "Comment id is required for update");
    Comment existing =
        commentRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + id));
    existing.setContent(comment.getContent());
    existing.setCreatedBy(comment.getCreatedBy());
    existing.setCreatedAt(comment.getCreatedAt());
    return commentRepository.save(existing);
  }

  @Override
  public void deleteComment(Long id) {
    commentRepository.deleteById(id);
  }

  @Override
  public int upvoteComment(Long id) {
    return changeCommentVotes(id, 1);
  }

  @Override
  public int downvoteComment(Long id) {
    return changeCommentVotes(id, -1);
  }

  private int changeCommentVotes(Long id, int delta) {
    Comment comment =
        commentRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + id));
    comment.setVotes(comment.getVotes() + delta);
    commentRepository.save(comment);
    return comment.getVotes();
  }

  private void ensureQuestionExists(Long questionId) {
    questionRepository
        .findById(questionId)
        .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
  }

  private Answer ensureAnswerExists(Long answerId) {
    return answerRepository
        .findById(answerId)
        .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + answerId));
  }
}
