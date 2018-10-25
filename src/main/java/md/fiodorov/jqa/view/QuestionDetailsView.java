package md.fiodorov.jqa.view;

import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionDetailsView extends QuestionListItemView {

  private String lastEditor;

  private Instant lastEditedDate;
}
