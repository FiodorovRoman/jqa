package md.fiodorov.jqa.service.impl;

import java.util.Objects;
import java.util.Optional;
import md.fiodorov.jqa.domain.Question;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.service.api.QuestionService;
import md.fiodorov.jqa.util.pagination.Page;
import md.fiodorov.jqa.util.pagination.PageRequest;
import md.fiodorov.jqa.util.pagination.Pageable;
import md.fiodorov.jqa.util.pagination.Sort;
import md.fiodorov.jqa.view.CreateUpdateQuestionView;
import md.fiodorov.jqa.view.QuestionDetailsView;
import md.fiodorov.jqa.view.QuestionListItemView;

public class QuestionServiceImpl implements QuestionService {

  private final QuestionRepository questionRepository;

  public QuestionServiceImpl(QuestionRepository questionRepository) {
    this.questionRepository = questionRepository;
  }

  @Override
  public Page<QuestionListItemView> getSortedAndPagedQuestionList(Sort sort, Pageable pageable) {
    Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    return questionRepository.findAll(sortedPageable).map(this::toListItemView);
  }

  @Override
  public Optional<QuestionDetailsView> getQuestionDetailsById(Long id) {
    return questionRepository.findById(id).map(this::toDetailsView);
  }

  @Override
  public void addQuestion(CreateUpdateQuestionView createQuestionView) {
    Question question = new Question();
    applyQuestionData(question, createQuestionView);
    questionRepository.save(question);
  }

  @Override
  public void deleteQuestionById(Long id) {
    questionRepository.deleteById(id);
  }

  @Override
  public void updateQuestion(CreateUpdateQuestionView createQuestionView) {
    Long id =
        Objects.requireNonNull(createQuestionView.getId(), "Question id is required for update");
    Question existingQuestion =
        questionRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
    applyQuestionData(existingQuestion, createQuestionView);
    questionRepository.save(existingQuestion);
  }

  @Override
  public int upvoteQuestion(Long id) {
    return changeQuestionRank(id, 1);
  }

  @Override
  public int downvoteQuestion(Long id) {
    return changeQuestionRank(id, -1);
  }

  private int changeQuestionRank(Long id, int delta) {
    Question question =
        questionRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
    question.setRank(question.getRank() + delta);
    questionRepository.save(question);
    return question.getRank();
  }

  private QuestionListItemView toListItemView(Question question) {
    QuestionListItemView view = new QuestionListItemView();
    populateCommonFields(question, view);
    return view;
  }

  private QuestionDetailsView toDetailsView(Question question) {
    QuestionDetailsView view = new QuestionDetailsView();
    populateCommonFields(question, view);
    User lastEditor = question.getEditedBy();
    view.setLastEditor(lastEditor != null ? lastEditor.getUsername() : null);
    view.setLastEditedDate(question.getEditedAt());
    return view;
  }

  private void populateCommonFields(Question question, QuestionListItemView view) {
    view.setId(question.getId());
    view.setTitle(question.getTitle());
    view.setContent(question.getContent());
    User author = question.getCreatedBy();
    view.setAuthor(author != null ? author.getUsername() : null);
    view.setRank(Integer.toString(question.getRank()));
    view.setCreatedDate(question.getCreatedAt());
  }

  private void applyQuestionData(Question question, CreateUpdateQuestionView view) {
    question.setTitle(view.getTitle());
    question.setContent(view.getContent());
    if (view.getCreatedDate() != null) {
      question.setCreatedAt(view.getCreatedDate());
    }
    if (view.getCreatedBy() != null) {
      question.setCreatedBy(view.getCreatedBy());
    }
  }
}
