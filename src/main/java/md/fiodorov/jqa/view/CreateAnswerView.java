package md.fiodorov.jqa.view;

import java.time.Instant;
import md.fiodorov.jqa.domain.User;

public class CreateAnswerView {
  private String content;
  private User createdBy;
  private Instant createdDate;

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Instant createdDate) {
    this.createdDate = createdDate;
  }
}
