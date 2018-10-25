package md.fiodorov.jqa.service.impl;

import java.util.Optional;
import md.fiodorov.jqa.repository.QuestionRepository;
import md.fiodorov.jqa.service.api.QuestionService;
import md.fiodorov.jqa.view.CreateUpdateQuestionView;
import md.fiodorov.jqa.view.QuestionDetailsView;
import md.fiodorov.jqa.view.QuestionListItemView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl implements QuestionService {

  private QuestionRepository questionRepository;

  @Autowired
  public QuestionServiceImpl(QuestionRepository questionRepository) {
    this.questionRepository = questionRepository;
  }


  @Override
  public Page<QuestionListItemView> getSortedAndPagedQuestionList(Sort sort, Pageable pageable) {
    return null;
  }

  @Override
  public Optional<QuestionDetailsView> getQuestionDetailsById(Long id) {
    return Optional.empty();
  }

  @Override
  public void addQuestion(CreateUpdateQuestionView createQuestionView) {

  }

  @Override
  public void deleteQuestionById(Long id) {

  }

  @Override
  public void updateQuestion(CreateUpdateQuestionView createQuestionView) {

  }
}
