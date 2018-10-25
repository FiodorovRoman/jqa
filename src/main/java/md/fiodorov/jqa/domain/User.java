package md.fiodorov.jqa.domain;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="users")
@Data
public class User extends BaseModel{

  private String username;

  private String password;

  private List<Question> questions;

  private List<Answer> answers;


}
