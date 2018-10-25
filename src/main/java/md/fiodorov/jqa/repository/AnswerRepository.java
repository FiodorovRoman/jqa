package md.fiodorov.jqa.repository;

import md.fiodorov.jqa.domain.Answer;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AnswerRepository extends PagingAndSortingRepository<Answer, Long> {
}
