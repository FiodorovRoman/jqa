package md.fiodorov.jqa.service.impl;

import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.domain.Question;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.AnswerRepository;
import md.fiodorov.jqa.repository.ConfigRepository;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.repository.UserRepository;
import md.fiodorov.jqa.util.pagination.Page;
import md.fiodorov.jqa.util.pagination.Pageable;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class VotingServiceImplTest {

  @Test
  void voteQuestion_updatesRankAndAuthorRating() {
    InMemQuestionRepo qRepo = new InMemQuestionRepo();
    InMemAnswerRepo aRepo = new InMemAnswerRepo();
    InMemUserRepo uRepo = new InMemUserRepo();
    InMemConfig cfg = new InMemConfig();
    cfg.setInt("points.question.upvote", 5);
    cfg.setInt("points.question.downvote", 2);

    User author = new User();
    author.setId(1L);
    author.setUsername("alice");
    author.setRating(10);
    uRepo.save(author);

    Question q = new Question();
    q.setId(100L);
    q.setTitle("Q");
    q.setCreatedBy(author);
    q.setRank(0);
    qRepo.save(q);

    VotingServiceImpl service = new VotingServiceImpl(qRepo, aRepo, uRepo, cfg);

    int rank1 = service.voteQuestionUp(100L);
    assertEquals(1, rank1);
    assertEquals(15, author.getRating(), "author should gain +5");

    int rank2 = service.voteQuestionDown(100L);
    assertEquals(0, rank2);
    assertEquals(13, author.getRating(), "author should lose -2");
  }

  @Test
  void voteAnswer_updatesRankAndAuthorRating_andAcceptanceAwardsPoints() {
    InMemQuestionRepo qRepo = new InMemQuestionRepo();
    InMemAnswerRepo aRepo = new InMemAnswerRepo();
    InMemUserRepo uRepo = new InMemUserRepo();
    InMemConfig cfg = new InMemConfig();
    cfg.setInt("points.answer.upvote", 3);
    cfg.setInt("points.answer.downvote", 1);
    cfg.setInt("points.answer.accepted", 8);

    User bob = new User();
    bob.setId(2L);
    bob.setUsername("bob");
    bob.setRating(0);
    uRepo.save(bob);

    Question q = new Question();
    q.setId(200L);
    qRepo.save(q);

    Answer a = new Answer();
    a.setId(201L);
    a.setQuestionId(200L);
    a.setCreatedBy(bob);
    a.setCreatedAt(Instant.now());
    aRepo.save(a);

    VotingServiceImpl service = new VotingServiceImpl(qRepo, aRepo, uRepo, cfg);

    int r1 = service.voteAnswerUp(201L);
    assertEquals(1, r1);
    assertEquals(3, bob.getRating(), "+3 for upvote");

    int r2 = service.voteAnswerDown(201L);
    assertEquals(0, r2);
    assertEquals(2, bob.getRating(), "-1 for downvote");

    // Acceptance points through AnswerServiceImpl
    AnswerServiceImpl answerService = new AnswerServiceImpl(aRepo, qRepo, uRepo, cfg);
    Answer right = answerService.markAsRight(201L);
    assertTrue(right.isRight());
    assertEquals(10, bob.getRating(), "+8 for accepted");
  }

  // Minimal in-memory helpers for unit test
  static class InMemQuestionRepo implements QuestionRepository {
    Map<Long, Question> map = new HashMap<>();
    @Override public Page<Question> findAll(Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public Optional<Question> findById(Long id) { return Optional.ofNullable(map.get(id)); }
    @Override public Question save(Question question) { if (question.getId()==null) question.setId((long) (map.size()+1)); map.put(question.getId(), question); return question; }
    @Override public void deleteById(Long id) { map.remove(id); }
  }
  static class InMemAnswerRepo implements AnswerRepository {
    Map<Long, Answer> map = new HashMap<>();
    @Override public Optional<Answer> findById(Long id) { return Optional.ofNullable(map.get(id)); }
    @Override public java.util.List<Answer> findByQuestionId(Long questionId) { throw new UnsupportedOperationException(); }
    @Override public Optional<Answer> findRightByQuestionId(Long questionId) { return map.values().stream().filter(a->Objects.equals(a.getQuestionId(),questionId)&&a.isRight()).findFirst(); }
    @Override public Answer save(Answer answer) { if (answer.getId()==null) answer.setId((long)(map.size()+1)); map.put(answer.getId(), answer); return answer; }
    @Override public void deleteById(Long id) { map.remove(id); }
    @Override public void unsetRightForQuestion(Long questionId) { map.values().forEach(a->{ if (Objects.equals(a.getQuestionId(), questionId)) a.setRight(false);}); }
  }
  static class InMemUserRepo implements UserRepository {
    Map<String, User> byUsername = new HashMap<>();
    @Override public Optional<User> findByUsername(String username) { return Optional.ofNullable(byUsername.get(username)); }
    @Override public Optional<User> findByGoogleId(String googleId) { return Optional.empty(); }
    @Override public Optional<User> findByFacebookId(String facebookId) { return Optional.empty(); }
    @Override public User save(User user) { if (user.getUsername()==null) user.setUsername("u"+byUsername.size()); byUsername.put(user.getUsername(), user); return user; }
  }
  static class InMemConfig implements ConfigRepository {
    Map<String,Integer> vals = new HashMap<>();
    @Override public int getInt(String key, int defaultValue) { return vals.getOrDefault(key, defaultValue); }
    @Override public void setInt(String key, int value) { vals.put(key, value); }
  }
}
