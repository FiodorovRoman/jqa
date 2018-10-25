package md.fiodorov.jqa.view;

import java.time.Instant;
import lombok.Data;

@Data
public class QuestionListItemView {

  private Long id;

  private String title;

  private String content;

  private String author;

  private String rank;

  private Instant createdDate;

}
