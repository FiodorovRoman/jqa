package md.fiodorov.jqa.repository;

import md.fiodorov.jqa.domain.Question;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface QuestionRepository extends PagingAndSortingRepository<Question, Long> {

}
