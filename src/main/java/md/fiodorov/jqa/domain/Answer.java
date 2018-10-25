package md.fiodorov.jqa.domain;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="answers")
@Data
public class Answer extends ContentModel implements Commentable {

  private Question question;

  private List<AnswerComment> comments;

}
