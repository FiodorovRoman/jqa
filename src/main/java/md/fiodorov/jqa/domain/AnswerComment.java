package md.fiodorov.jqa.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="answer_comments")
@Data
public class AnswerComment extends ContentModel implements Comment{

  private Answer parent;

}
