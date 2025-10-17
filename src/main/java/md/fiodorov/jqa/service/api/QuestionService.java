package md.fiodorov.jqa.service.api;

import java.util.Optional;
import md.fiodorov.jqa.util.pagination.Page;
import md.fiodorov.jqa.util.pagination.Pageable;
import md.fiodorov.jqa.util.pagination.Sort;
import md.fiodorov.jqa.view.CreateUpdateQuestionView;
import md.fiodorov.jqa.view.QuestionDetailsView;
import md.fiodorov.jqa.view.QuestionListItemView;

public interface QuestionService {

  Page<QuestionListItemView> getSortedAndPagedQuestionList(Sort sort, Pageable pageable);

  Optional<QuestionDetailsView> getQuestionDetailsById(Long id);

  void addQuestion(CreateUpdateQuestionView createQuestionView);

  void deleteQuestionById(Long id);

  void updateQuestion(CreateUpdateQuestionView createQuestionView);

  int upvoteQuestion(Long id);

  int downvoteQuestion(Long id);
}
