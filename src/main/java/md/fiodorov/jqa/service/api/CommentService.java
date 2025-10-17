package md.fiodorov.jqa.service.api;

import java.util.List;
import md.fiodorov.jqa.domain.Comment;

public interface CommentService {

  List<Comment> getCommentsForQuestion(Long questionId);

  List<Comment> getCommentsForAnswer(Long answerId);

  Comment getCommentById(Long id);

  Comment addCommentToQuestion(Long questionId, Comment comment);

  Comment addCommentToAnswer(Long answerId, Comment comment);

  Comment updateComment(Comment comment);

  void deleteComment(Long id);

  int upvoteComment(Long id);

  int downvoteComment(Long id);
}
