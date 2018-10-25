package md.fiodorov.jqa.service.api;

import java.util.Optional;
import md.fiodorov.jqa.view.CreateUpdateQuestionView;
import md.fiodorov.jqa.view.QuestionDetailsView;
import md.fiodorov.jqa.view.QuestionListItemView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface QuestionService {

  Page<QuestionListItemView> getSortedAndPagedQuestionList(Sort sort, Pageable pageable);

  Optional<QuestionDetailsView> getQuestionDetailsById(Long id);

  void addQuestion(CreateUpdateQuestionView createQuestionView);

  void deleteQuestionById(Long id);

  void updateQuestion(CreateUpdateQuestionView createQuestionView);


}
