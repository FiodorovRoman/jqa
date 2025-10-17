package md.fiodorov.jqa.controller;

import java.util.List;
import md.fiodorov.jqa.domain.Comment;
import md.fiodorov.jqa.service.api.CommentService;

public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  public List<Comment> listQuestionComments(Long questionId) {
    return commentService.getCommentsForQuestion(questionId);
  }

  public List<Comment> listAnswerComments(Long answerId) {
    return commentService.getCommentsForAnswer(answerId);
  }

  public Comment getComment(Long id) {
    return commentService.getCommentById(id);
  }

  public Comment createQuestionComment(Long questionId, Comment comment) {
    return commentService.addCommentToQuestion(questionId, comment);
  }

  public Comment createAnswerComment(Long answerId, Comment comment) {
    return commentService.addCommentToAnswer(answerId, comment);
  }

  public Comment updateComment(Comment comment) {
    return commentService.updateComment(comment);
  }

  public void deleteComment(Long id) {
    commentService.deleteComment(id);
  }

  public int upvoteComment(Long id) {
    return commentService.upvoteComment(id);
  }

  public int downvoteComment(Long id) {
    return commentService.downvoteComment(id);
  }
}
