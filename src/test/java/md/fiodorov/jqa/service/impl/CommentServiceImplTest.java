package md.fiodorov.jqa.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.domain.Comment;
import md.fiodorov.jqa.domain.Question;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.AnswerRepository;
import md.fiodorov.jqa.repository.CommentRepository;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.util.pagination.Page;
import md.fiodorov.jqa.util.pagination.PageRequest;
import md.fiodorov.jqa.util.pagination.Pageable;
import md.fiodorov.jqa.util.pagination.Sort;

public class CommentServiceImplTest {

  public static void main(String[] args) {
    CommentServiceImplTest test = new CommentServiceImplTest();
    test.getCommentsForQuestion_returnsAllLinkedComments();
    test.getCommentsForAnswer_returnsAllLinkedComments();
    test.addCommentToAnswer_linksQuestionIdentifier();
    test.updateComment_updatesPersistedData();
    test.voteComment_adjustsStoredCount();
    test.deleteComment_removesFromRepository();
    System.out.println("All CommentServiceImpl tests passed");
  }

  private void getCommentsForQuestion_returnsAllLinkedComments() {
    InMemoryCommentRepository commentRepository = new InMemoryCommentRepository();
    InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    questionRepository.storeQuestion(sampleQuestion(1L));
    CommentServiceImpl service =
        new CommentServiceImpl(commentRepository, questionRepository, answerRepository);

    Comment first = sampleQuestionComment(10L, 1L, "First comment");
    Comment second = sampleQuestionComment(11L, 1L, "Second comment");
    commentRepository.save(first);
    commentRepository.save(second);

    List<Comment> result = service.getCommentsForQuestion(1L);

    assertEquals(2, result.size(), "Should return two comments");
    assertTrue(result.contains(first), "Result should contain first comment");
    assertTrue(result.contains(second), "Result should contain second comment");
  }

  private void getCommentsForAnswer_returnsAllLinkedComments() {
    InMemoryCommentRepository commentRepository = new InMemoryCommentRepository();
    InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    Question question = sampleQuestion(2L);
    questionRepository.storeQuestion(question);
    Answer answer = sampleAnswer(20L, 2L);
    answerRepository.save(answer);
    CommentServiceImpl service =
        new CommentServiceImpl(commentRepository, questionRepository, answerRepository);

    Comment first = sampleAnswerComment(12L, 2L, 20L, "Answer comment");
    commentRepository.save(first);

    List<Comment> result = service.getCommentsForAnswer(20L);

    assertEquals(1, result.size(), "Should return one comment");
    assertTrue(result.contains(first), "Result should contain stored comment");
  }

  private void addCommentToAnswer_linksQuestionIdentifier() {
    InMemoryCommentRepository commentRepository = new InMemoryCommentRepository();
    InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    Question question = sampleQuestion(3L);
    questionRepository.storeQuestion(question);
    Answer answer = sampleAnswer(30L, 3L);
    answerRepository.save(answer);
    CommentServiceImpl service =
        new CommentServiceImpl(commentRepository, questionRepository, answerRepository);

    Comment toCreate = new Comment();
    toCreate.setContent("Reply to answer");
    User author = new User();
    author.setId(5L);
    author.setUsername("erin");
    toCreate.setCreatedBy(author);
    toCreate.setCreatedAt(Instant.parse("2024-05-15T08:45:00Z"));

    Comment saved = service.addCommentToAnswer(30L, toCreate);

    assertEquals(Long.valueOf(30L), saved.getAnswerId(), "Answer id should be set");
    assertEquals(Long.valueOf(3L), saved.getQuestionId(), "Question id should be inherited");
  }

  private void updateComment_updatesPersistedData() {
    InMemoryCommentRepository commentRepository = new InMemoryCommentRepository();
    InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    questionRepository.storeQuestion(sampleQuestion(4L));
    CommentServiceImpl service =
        new CommentServiceImpl(commentRepository, questionRepository, answerRepository);

    Comment existing = sampleQuestionComment(40L, 4L, "Original");
    commentRepository.save(existing);

    Comment toUpdate = new Comment();
    toUpdate.setId(40L);
    toUpdate.setContent("Updated");
    User editor = new User();
    editor.setId(6L);
    editor.setUsername("frank");
    toUpdate.setCreatedBy(editor);
    toUpdate.setCreatedAt(Instant.parse("2024-05-16T10:00:00Z"));

    Comment persisted = service.updateComment(toUpdate);

    assertEquals("Updated", persisted.getContent(), "Content should be updated");
    assertEquals(editor, persisted.getCreatedBy(), "Author should be updated");
    assertEquals(toUpdate.getCreatedAt(), persisted.getCreatedAt(), "Timestamp should be updated");
  }

  private void voteComment_adjustsStoredCount() {
    InMemoryCommentRepository commentRepository = new InMemoryCommentRepository();
    InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    questionRepository.storeQuestion(sampleQuestion(5L));
    CommentServiceImpl service =
        new CommentServiceImpl(commentRepository, questionRepository, answerRepository);

    Comment comment = sampleQuestionComment(50L, 5L, "Please vote");
    comment.setVotes(2);
    commentRepository.save(comment);

    int upvotes = service.upvoteComment(50L);
    int downvotes = service.downvoteComment(50L);

    assertEquals(3, upvotes, "Upvote should increment votes");
    assertEquals(2, downvotes, "Downvote should decrement votes");
  }

  private void deleteComment_removesFromRepository() {
    InMemoryCommentRepository commentRepository = new InMemoryCommentRepository();
    InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    questionRepository.storeQuestion(sampleQuestion(6L));
    CommentServiceImpl service =
        new CommentServiceImpl(commentRepository, questionRepository, answerRepository);

    Comment comment = sampleQuestionComment(60L, 6L, "Delete me");
    commentRepository.save(comment);

    service.deleteComment(60L);

    assertTrue(!commentRepository.comments.containsKey(60L), "Comment should be removed");
  }

  private Comment sampleQuestionComment(Long id, Long questionId, String content) {
    Comment comment = new Comment();
    comment.setId(id);
    comment.setQuestionId(questionId);
    comment.setContent(content);
    return comment;
  }

  private Comment sampleAnswerComment(Long id, Long questionId, Long answerId, String content) {
    Comment comment = sampleQuestionComment(id, questionId, content);
    comment.setAnswerId(answerId);
    return comment;
  }

  private Question sampleQuestion(Long id) {
    Question question = new Question();
    question.setId(id);
    question.setTitle("Question " + id);
    question.setRank(0);
    return question;
  }

  private Answer sampleAnswer(Long id, Long questionId) {
    Answer answer = new Answer();
    answer.setId(id);
    answer.setQuestionId(questionId);
    return answer;
  }

  private static void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  private static void assertEquals(Object expected, Object actual, String message) {
    if (expected == null ? actual != null : !expected.equals(actual)) {
      throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
    }
  }

  private static final class InMemoryCommentRepository implements CommentRepository {

    private final Map<Long, Comment> comments = new HashMap<>();

    @Override
    public List<Comment> findByQuestionId(Long questionId) {
      List<Comment> result = new ArrayList<>();
      for (Comment comment : comments.values()) {
        if (questionId.equals(comment.getQuestionId())) {
          result.add(comment);
        }
      }
      return result;
    }

    @Override
    public List<Comment> findByAnswerId(Long answerId) {
      List<Comment> result = new ArrayList<>();
      for (Comment comment : comments.values()) {
        if (answerId.equals(comment.getAnswerId())) {
          result.add(comment);
        }
      }
      return result;
    }

    @Override
    public Optional<Comment> findById(Long id) {
      return Optional.ofNullable(comments.get(id));
    }

    @Override
    public Comment save(Comment comment) {
      if (comment.getId() == null) {
        comment.setId((long) (comments.size() + 1));
      }
      comments.put(comment.getId(), comment);
      return comment;
    }

    @Override
    public void deleteById(Long id) {
      comments.remove(id);
    }
  }

  private static final class InMemoryQuestionRepository implements QuestionRepository {

    private final Map<Long, Question> questions = new HashMap<>();

    void storeQuestion(Question question) {
      questions.put(question.getId(), question);
    }

    @Override
    public Page<Question> findAll(Pageable pageable) {
      return new Page<>(List.of(), PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id")));
    }

    @Override
    public Optional<Question> findById(Long id) {
      return Optional.ofNullable(questions.get(id));
    }

    @Override
    public Question save(Question question) {
      questions.put(question.getId(), question);
      return question;
    }

    @Override
    public void deleteById(Long id) {
      questions.remove(id);
    }
  }

  private static final class InMemoryAnswerRepository implements AnswerRepository {

    private final Map<Long, Answer> answers = new HashMap<>();

    @Override
    public List<Answer> findByQuestionId(Long questionId) {
      List<Answer> result = new ArrayList<>();
      for (Answer answer : answers.values()) {
        if (questionId.equals(answer.getQuestionId())) {
          result.add(answer);
        }
      }
      return result;
    }

    @Override
    public Optional<Answer> findById(Long id) {
      return Optional.ofNullable(answers.get(id));
    }

    @Override
    public Answer save(Answer answer) {
      if (answer.getId() == null) {
        answer.setId((long) (answers.size() + 1));
      }
      answers.put(answer.getId(), answer);
      return answer;
    }

    @Override
    public void deleteById(Long id) {
      answers.remove(id);
    }
  }
}
