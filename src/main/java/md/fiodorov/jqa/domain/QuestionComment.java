package md.fiodorov.jqa.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="question_comments")
@Data
public class QuestionComment extends ContentModel implements Comment{

  private Question parent;

}
