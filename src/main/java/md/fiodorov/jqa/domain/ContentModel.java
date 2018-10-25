package md.fiodorov.jqa.domain;

import java.util.List;
import javax.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass
@Data
abstract class ContentModel {

  private String content;

  private User createdBy;

  private User editedBy;

  private List<User> votedBy;
}
