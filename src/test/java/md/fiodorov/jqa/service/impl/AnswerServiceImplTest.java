package md.fiodorov.jqa.service.impl;

import java.time.Instant;
import java.util.*;
import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.domain.Question;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.AnswerRepository;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.service.api.AnswerService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AnswerServiceImplTest {

  @Test
  void addAndListAnswers_work() {
    InMemoryAnswerRepository answerRepo = new InMemoryAnswerRepository();
    RecordingQuestionRepository questionRepo = new RecordingQuestionRepository();
    Question q = sampleQuestion(1L);
    questionRepo.store(q);
    AnswerService service = new AnswerServiceImpl(answerRepo, questionRepo);

    User alice = new User();
    alice.setId(10L);
    alice.setUsername("alice");

    Answer a1 = service.addAnswer(q.getId(), "First", alice, Instant.parse("2024-01-01T00:00:00Z"));
    Answer a2 = service.addAnswer(q.getId(), "Second", alice, Instant.parse("2024-01-02T00:00:00Z"));

    assertNotNull(a1.getId(), "id should be assigned");
    assertNotNull(a2.getId(), "id should be assigned");
    assertEquals(2, service.listAnswers(q.getId()).size(), "two answers expected");
  }

  @Test
  void markAsRight_enforcesSingleRightAndClosesQuestion() {
    InMemoryAnswerRepository answerRepo = new InMemoryAnswerRepository();
    RecordingQuestionRepository questionRepo = new RecordingQuestionRepository();
    Question q = sampleQuestion(2L);
    questionRepo.store(q);
    AnswerService service = new AnswerServiceImpl(answerRepo, questionRepo);

    User bob = new User();
    bob.setId(11L);
    bob.setUsername("bob");

    Answer a1 = service.addAnswer(q.getId(), "A1", bob, Instant.parse("2024-02-01T00:00:00Z"));
    Answer a2 = service.addAnswer(q.getId(), "A2", bob, Instant.parse("2024-02-02T00:00:00Z"));

    Answer right1 = service.markAsRight(a1.getId());
    assertTrue(right1.isRight(), "first marked should be right");
    assertTrue(questionRepo.lastSaved.isClosed(), "question should be closed after marking right");

    Answer right2 = service.markAsRight(a2.getId());
    assertTrue(right2.isRight(), "second marked should be right");

    // Ensure only one right in repository state
    Optional<Answer> repoRight = answerRepo.findRightByQuestionId(q.getId());
    assertTrue(repoRight.isPresent(), "right answer should exist");
    assertEquals(a2.getId(), repoRight.get().getId(), "right answer must be the last selected");
  }

  private Question sampleQuestion(Long id) {
    Question q = new Question();
    q.setId(id);
    q.setTitle("T");
    q.setContent("C");
    q.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    q.setRank(0);
    return q;
  }

  private static final class RecordingQuestionRepository implements QuestionRepository {
    private final Map<Long, Question> store = new HashMap<>();
    private Question lastSaved;

    void store(Question q) { store.put(q.getId(), q); }

    @Override
    public md.fiodorov.jqa.util.pagination.Page<Question> findAll(md.fiodorov.jqa.util.pagination.Pageable pageable) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Question> findById(Long id) {
      return Optional.ofNullable(store.get(id));
    }

    @Override
    public Question save(Question question) {
      store.put(question.getId(), question);
      lastSaved = question;
      return question;
    }

    @Override
    public void deleteById(Long id) {
      store.remove(id);
    }
  }

  private static final class InMemoryAnswerRepository implements AnswerRepository {
    private final Map<Long, Answer> answers = new HashMap<>();
    private long seq = 1;

    @Override
    public Optional<Answer> findById(Long id) {
      return Optional.ofNullable(answers.get(id));
    }

    @Override
    public List<Answer> findByQuestionId(Long questionId) {
      List<Answer> list = new ArrayList<>();
      for (Answer a : answers.values()) {
        if (Objects.equals(a.getQuestionId(), questionId)) {
          list.add(copy(a));
        }
      }
      // stable order by id
      list.sort(Comparator.comparing(Answer::getId));
      return list;
    }

    @Override
    public Optional<Answer> findRightByQuestionId(Long questionId) {
      return answers.values().stream()
          .filter(a -> Objects.equals(a.getQuestionId(), questionId) && a.isRight())
          .findFirst()
          .map(this::copy);
    }

    @Override
    public Answer save(Answer answer) {
      if (answer.getId() == null) {
        answer.setId(seq++);
      }
      answers.put(answer.getId(), copy(answer));
      return copy(answer);
    }

    @Override
    public void deleteById(Long id) {
      answers.remove(id);
    }

    @Override
    public void unsetRightForQuestion(Long questionId) {
      for (Answer a : answers.values()) {
        if (Objects.equals(a.getQuestionId(), questionId) && a.isRight()) {
          a.setRight(false);
        }
      }
    }

    private Answer copy(Answer src) {
      Answer a = new Answer();
      a.setId(src.getId());
      a.setQuestionId(src.getQuestionId());
      a.setContent(src.getContent());
      a.setCreatedBy(src.getCreatedBy());
      a.setCreatedAt(src.getCreatedAt());
      a.setRight(src.isRight());
      return a;
    }
  }
}
