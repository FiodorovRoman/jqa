package md.fiodorov.jqa.domain;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="questions")
@Data
public class Question extends ContentModel implements Commentable {

  private String title;

  private List<Answer> answers;

  private List<QuestionComment> comments;
}
