package md.fiodorov.jqa.controller;

import java.util.Optional;
import md.fiodorov.jqa.service.api.QuestionService;
import md.fiodorov.jqa.util.pagination.Page;
import md.fiodorov.jqa.util.pagination.Pageable;
import md.fiodorov.jqa.util.pagination.Sort;
import md.fiodorov.jqa.view.CreateUpdateQuestionView;
import md.fiodorov.jqa.view.QuestionDetailsView;
import md.fiodorov.jqa.view.QuestionListItemView;

public class QuestionController {

  private final QuestionService questionService;

  public QuestionController(QuestionService questionService) {
    this.questionService = questionService;
  }

  public Page<QuestionListItemView> listQuestions(Sort sort, Pageable pageable) {
    return questionService.getSortedAndPagedQuestionList(sort, pageable);
  }

  public Optional<QuestionDetailsView> getQuestion(Long id) {
    return questionService.getQuestionDetailsById(id);
  }

  public void createQuestion(CreateUpdateQuestionView view) {
    questionService.addQuestion(view);
  }

  public void updateQuestion(CreateUpdateQuestionView view) {
    questionService.updateQuestion(view);
  }

  public void deleteQuestion(Long id) {
    questionService.deleteQuestionById(id);
  }

  public int upvoteQuestion(Long id) {
    return questionService.upvoteQuestion(id);
  }

  public int downvoteQuestion(Long id) {
    return questionService.downvoteQuestion(id);
  }
}
