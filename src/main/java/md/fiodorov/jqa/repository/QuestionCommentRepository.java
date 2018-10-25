package md.fiodorov.jqa.repository;

import md.fiodorov.jqa.domain.QuestionComment;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface QuestionCommentRepository extends PagingAndSortingRepository<QuestionComment, Long> {

}
