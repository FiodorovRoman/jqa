package md.fiodorov.jqa.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import md.fiodorov.jqa.domain.Question;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.util.pagination.Page;
import md.fiodorov.jqa.util.pagination.PageRequest;
import md.fiodorov.jqa.util.pagination.Pageable;
import md.fiodorov.jqa.util.pagination.Sort;
import md.fiodorov.jqa.view.CreateUpdateQuestionView;
import md.fiodorov.jqa.view.QuestionDetailsView;
import md.fiodorov.jqa.view.QuestionListItemView;

public class QuestionServiceImplTest {

  public static void main(String[] args) {
    QuestionServiceImplTest test = new QuestionServiceImplTest();
    test.getSortedAndPagedQuestionList_returnsMappedPage();
    test.getQuestionDetailsById_returnsDetailedViewWhenPresent();
    test.getQuestionDetailsById_returnsEmptyWhenMissing();
    test.addQuestion_persistsMappedEntity();
    test.deleteQuestionById_delegatesToRepository();
    test.updateQuestion_updatesExistingEntity();
    test.updateQuestion_throwsWhenIdMissing();
    test.updateQuestion_throwsWhenQuestionMissing();
    test.upvoteQuestion_incrementsRank();
    test.downvoteQuestion_decrementsRank();
    System.out.println("All QuestionServiceImpl tests passed");
  }

  private void getSortedAndPagedQuestionList_returnsMappedPage() {
    RecordingQuestionRepository repository = new RecordingQuestionRepository();
    QuestionServiceImpl service = new QuestionServiceImpl(repository);
    Question question = createSampleQuestion();
    repository.pageResult =
        new Page<>(
            List.of(question),
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));

    Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
    Page<QuestionListItemView> result =
        service.getSortedAndPagedQuestionList(sort, PageRequest.of(0, 5));

    assertTrue(result.getContent().size() == 1, "Expected single list item");
    QuestionListItemView itemView = result.getContent().get(0);
    assertEquals(question.getId(), itemView.getId(), "List item id");
    assertEquals(question.getTitle(), itemView.getTitle(), "List item title");
    assertEquals(question.getContent(), itemView.getContent(), "List item content");
    assertEquals(
        question.getCreatedBy().getUsername(), itemView.getAuthor(), "List item author");
    assertEquals(Integer.toString(question.getRank()), itemView.getRank(), "List item rank");
    assertEquals(question.getCreatedAt(), itemView.getCreatedDate(), "List item created date");
    assertEquals(sort, repository.lastPageable.getSort(), "Repository sort should match");
  }

  private void getQuestionDetailsById_returnsDetailedViewWhenPresent() {
    RecordingQuestionRepository repository = new RecordingQuestionRepository();
    QuestionServiceImpl service = new QuestionServiceImpl(repository);
    Question question = createSampleQuestion();
    repository.findByIdResult = Optional.of(question);

    Optional<QuestionDetailsView> result = service.getQuestionDetailsById(question.getId());

    assertTrue(result.isPresent(), "Expected details to be present");
    QuestionDetailsView detailsView = result.orElseThrow();
    assertEquals(question.getId(), detailsView.getId(), "Details id");
    assertEquals(question.getTitle(), detailsView.getTitle(), "Details title");
    assertEquals(question.getEditedBy().getUsername(), detailsView.getLastEditor(), "Last editor");
    assertEquals(question.getEditedAt(), detailsView.getLastEditedDate(), "Last edit date");
  }

  private void getQuestionDetailsById_returnsEmptyWhenMissing() {
    RecordingQuestionRepository repository = new RecordingQuestionRepository();
    QuestionServiceImpl service = new QuestionServiceImpl(repository);
    repository.findByIdResult = Optional.empty();

    Optional<QuestionDetailsView> result = service.getQuestionDetailsById(99L);

    assertTrue(result.isEmpty(), "Expected empty optional when question missing");
  }

  private void addQuestion_persistsMappedEntity() {
    RecordingQuestionRepository repository = new RecordingQuestionRepository();
    QuestionServiceImpl service = new QuestionServiceImpl(repository);
    CreateUpdateQuestionView view = new CreateUpdateQuestionView();
    view.setTitle("New question");
    view.setContent("New content");
    view.setCreatedDate(Instant.parse("2024-03-01T00:00:00Z"));
    User author = new User();
    author.setId(1L);
    author.setUsername("alice");
    view.setCreatedBy(author);

    service.addQuestion(view);

    Question saved = repository.savedQuestion;
    assertTrue(saved != null, "Expected repository to save a question");
    assertEquals(view.getTitle(), saved.getTitle(), "Saved title");
    assertEquals(view.getContent(), saved.getContent(), "Saved content");
    assertEquals(view.getCreatedDate(), saved.getCreatedAt(), "Saved created date");
    assertEquals(author, saved.getCreatedBy(), "Saved author");
  }

  private void deleteQuestionById_delegatesToRepository() {
    RecordingQuestionRepository repository = new RecordingQuestionRepository();
    QuestionServiceImpl service = new QuestionServiceImpl(repository);

    service.deleteQuestionById(42L);

    assertEquals(Long.valueOf(42L), repository.deletedId, "Deleted id should match input");
  }

  private void updateQuestion_updatesExistingEntity() {
    RecordingQuestionRepository repository = new RecordingQuestionRepository();
    QuestionServiceImpl service = new QuestionServiceImpl(repository);
    Question existing = createSampleQuestion();
    repository.findByIdResult = Optional.of(existing);
    CreateUpdateQuestionView view = new CreateUpdateQuestionView();
    view.setId(existing.getId());
    view.setTitle("Updated title");
    view.setContent("Updated content");
    view.setCreatedDate(Instant.parse("2024-04-01T10:00:00Z"));
    User editor = new User();
    editor.setId(2L);
    editor.setUsername("bob");
    view.setCreatedBy(editor);

    service.updateQuestion(view);

    Question saved = repository.savedQuestion;
    assertTrue(saved != null, "Expected repository to save updated question");
    assertEquals(existing.getId(), saved.getId(), "Updated question id");
    assertEquals(view.getTitle(), saved.getTitle(), "Updated title");
    assertEquals(view.getContent(), saved.getContent(), "Updated content");
    assertEquals(view.getCreatedDate(), saved.getCreatedAt(), "Updated created date");
    assertEquals(editor, saved.getCreatedBy(), "Updated created by");
  }

  private void updateQuestion_throwsWhenIdMissing() {
    RecordingQuestionRepository repository = new RecordingQuestionRepository();
    QuestionServiceImpl service = new QuestionServiceImpl(repository);
    CreateUpdateQuestionView view = new CreateUpdateQuestionView();

    try {
      service.updateQuestion(view);
      throw new AssertionError("Expected NullPointerException when id is missing");
    } catch (NullPointerException ex) {
      assertTrue(
          ex.getMessage().contains("Question id is required for update"),
          "Exception message should mention missing id");
    }
  }

  private void updateQuestion_throwsWhenQuestionMissing() {
    RecordingQuestionRepository repository = new RecordingQuestionRepository();
    QuestionServiceImpl service = new QuestionServiceImpl(repository);
    CreateUpdateQuestionView view = new CreateUpdateQuestionView();
    view.setId(99L);
    repository.findByIdResult = Optional.empty();

    try {
      service.updateQuestion(view);
      throw new AssertionError("Expected IllegalArgumentException when question missing");
    } catch (IllegalArgumentException ex) {
      assertTrue(
          ex.getMessage().contains("Question not found: 99"),
          "Exception message should mention missing id");
    }
  }

  private void upvoteQuestion_incrementsRank() {
    RecordingQuestionRepository repository = new RecordingQuestionRepository();
    QuestionServiceImpl service = new QuestionServiceImpl(repository);
    Question existing = createSampleQuestion();
    existing.setRank(3);
    repository.findByIdResult = Optional.of(existing);

    int updatedRank = service.upvoteQuestion(existing.getId());

    assertEquals(4, updatedRank, "Upvoting should increment rank");
    assertTrue(repository.savedQuestion == existing, "Existing question should be saved");
  }

  private void downvoteQuestion_decrementsRank() {
    RecordingQuestionRepository repository = new RecordingQuestionRepository();
    QuestionServiceImpl service = new QuestionServiceImpl(repository);
    Question existing = createSampleQuestion();
    existing.setRank(2);
    repository.findByIdResult = Optional.of(existing);

    int updatedRank = service.downvoteQuestion(existing.getId());

    assertEquals(1, updatedRank, "Downvoting should decrement rank");
    assertTrue(repository.savedQuestion == existing, "Existing question should be saved");
  }

  private Question createSampleQuestion() {
    User author = new User();
    author.setId(1L);
    author.setUsername("alice");

    User editor = new User();
    editor.setId(2L);
    editor.setUsername("bob");

    Question question = new Question();
    question.setId(10L);
    question.setTitle("Sample title");
    question.setContent("Question body");
    question.setCreatedBy(author);
    question.setCreatedAt(Instant.parse("2024-01-01T10:15:30.00Z"));
    question.setRank(7);
    question.setEditedBy(editor);
    question.setEditedAt(Instant.parse("2024-02-01T12:00:00.00Z"));
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

  private static final class RecordingQuestionRepository implements QuestionRepository {

    private Page<Question> pageResult;
    private Optional<Question> findByIdResult = Optional.empty();
    private Question savedQuestion;
    private Long deletedId;
    private Pageable lastPageable;

    @Override
    public Page<Question> findAll(Pageable pageable) {
      this.lastPageable = pageable;
      return pageResult;
    }

    @Override
    public Optional<Question> findById(Long id) {
      return findByIdResult;
    }

    @Override
    public Question save(Question question) {
      this.savedQuestion = question;
      return question;
    }

    @Override
    public void deleteById(Long id) {
      this.deletedId = id;
    }
  }
}
