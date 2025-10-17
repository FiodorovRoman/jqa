package md.fiodorov.jqa.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.domain.Question;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.AnswerRepository;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.util.pagination.Page;
import md.fiodorov.jqa.util.pagination.PageRequest;
import md.fiodorov.jqa.util.pagination.Pageable;
import md.fiodorov.jqa.util.pagination.Sort;

public class AnswerServiceImplTest {

  public static void main(String[] args) {
    AnswerServiceImplTest test = new AnswerServiceImplTest();
    test.getAnswersForQuestion_returnsAnswers();
    test.addAnswer_linksToQuestion();
    test.updateAnswer_updatesExistingAnswer();
    test.voteAnswer_adjustsVoteCount();
    test.deleteAnswer_removesFromRepository();
    System.out.println("All AnswerServiceImpl tests passed");
  }

  private void getAnswersForQuestion_returnsAnswers() {
    InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    questionRepository.storeQuestion(sampleQuestion(1L));
    AnswerServiceImpl service = new AnswerServiceImpl(answerRepository, questionRepository);

    Answer first = sampleAnswer(10L, 1L, "First answer");
    Answer second = sampleAnswer(11L, 1L, "Second answer");
    answerRepository.save(first);
    answerRepository.save(second);

    List<Answer> answers = service.getAnswersForQuestion(1L);

    assertEquals(2, answers.size(), "Should return two answers");
    assertTrue(answers.contains(first), "Result should contain first answer");
    assertTrue(answers.contains(second), "Result should contain second answer");
  }

  private void addAnswer_linksToQuestion() {
    InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    questionRepository.storeQuestion(sampleQuestion(2L));
    AnswerServiceImpl service = new AnswerServiceImpl(answerRepository, questionRepository);

    Answer toCreate = new Answer();
    toCreate.setContent("New answer");
    User author = new User();
    author.setId(3L);
    author.setUsername("carol");
    toCreate.setCreatedBy(author);
    toCreate.setCreatedAt(Instant.parse("2024-05-10T12:00:00Z"));

    Answer saved = service.addAnswer(2L, toCreate);

    assertEquals(Long.valueOf(2L), saved.getQuestionId(), "Answer should be linked to question");
    assertTrue(answerRepository.answers.containsValue(saved), "Repository should store answer");
  }

  private void updateAnswer_updatesExistingAnswer() {
    InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    questionRepository.storeQuestion(sampleQuestion(3L));
    AnswerServiceImpl service = new AnswerServiceImpl(answerRepository, questionRepository);

    Answer existing = sampleAnswer(20L, 3L, "Initial content");
    answerRepository.save(existing);

    Answer updated = new Answer();
    updated.setId(20L);
    updated.setContent("Updated content");
    User editor = new User();
    editor.setId(4L);
    editor.setUsername("dave");
    updated.setCreatedBy(editor);
    updated.setCreatedAt(Instant.parse("2024-05-12T09:30:00Z"));

    Answer persisted = service.updateAnswer(updated);

    assertEquals("Updated content", persisted.getContent(), "Content should be updated");
    assertEquals(editor, persisted.getCreatedBy(), "Author should be updated");
    assertEquals(updated.getCreatedAt(), persisted.getCreatedAt(), "Created date should be updated");
  }

  private void voteAnswer_adjustsVoteCount() {
    InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    questionRepository.storeQuestion(sampleQuestion(4L));
    AnswerServiceImpl service = new AnswerServiceImpl(answerRepository, questionRepository);

    Answer answer = sampleAnswer(30L, 4L, "Vote me");
    answer.setVotes(1);
    answerRepository.save(answer);

    int upvoted = service.upvoteAnswer(30L);
    int downvoted = service.downvoteAnswer(30L);

    assertEquals(2, upvoted, "Upvote should increment votes");
    assertEquals(1, downvoted, "Downvote should decrement votes");
  }

  private void deleteAnswer_removesFromRepository() {
    InMemoryAnswerRepository answerRepository = new InMemoryAnswerRepository();
    InMemoryQuestionRepository questionRepository = new InMemoryQuestionRepository();
    questionRepository.storeQuestion(sampleQuestion(5L));
    AnswerServiceImpl service = new AnswerServiceImpl(answerRepository, questionRepository);

    Answer answer = sampleAnswer(40L, 5L, "To delete");
    answerRepository.save(answer);

    service.deleteAnswer(40L);

    assertTrue(!answerRepository.answers.containsKey(40L), "Answer should be removed");
  }

  private Answer sampleAnswer(Long id, Long questionId, String content) {
    Answer answer = new Answer();
    answer.setId(id);
    answer.setQuestionId(questionId);
    answer.setContent(content);
    return answer;
  }

  private Question sampleQuestion(Long id) {
    Question question = new Question();
    question.setId(id);
    question.setTitle("Question " + id);
    question.setRank(0);
    return question;
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
}
