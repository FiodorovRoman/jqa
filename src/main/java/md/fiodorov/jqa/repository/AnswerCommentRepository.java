package md.fiodorov.jqa.repository;

import md.fiodorov.jqa.domain.AnswerComment;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AnswerCommentRepository extends PagingAndSortingRepository<AnswerComment, Long> {

}
