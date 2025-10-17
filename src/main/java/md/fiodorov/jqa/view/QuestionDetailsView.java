package md.fiodorov.jqa.view;

import java.time.Instant;

public class QuestionDetailsView extends QuestionListItemView {

  private String lastEditor;
  private Instant lastEditedDate;

  public String getLastEditor() {
    return lastEditor;
  }

  public void setLastEditor(String lastEditor) {
    this.lastEditor = lastEditor;
  }

  public Instant getLastEditedDate() {
    return lastEditedDate;
  }

  public void setLastEditedDate(Instant lastEditedDate) {
    this.lastEditedDate = lastEditedDate;
  }
}
