package md.fiodorov.jqa.view;

import java.time.Instant;
import lombok.Data;
import md.fiodorov.jqa.domain.User;

@Data
public class CreateUpdateQuestionView {

  private String title;

  private String content;

  private Instant createdDate;

  private User createdBy;
}
