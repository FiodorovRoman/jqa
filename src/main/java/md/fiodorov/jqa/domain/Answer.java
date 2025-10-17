package md.fiodorov.jqa.domain;

import java.time.Instant;

public class Answer {

  private Long id;
  private Long questionId;
  private String content;
  private User createdBy;
  private Instant createdAt;
  private Instant editedAt;
  private User editedBy;
  private int votes;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getQuestionId() {
    return questionId;
  }

  public void setQuestionId(Long questionId) {
    this.questionId = questionId;
  }

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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getEditedAt() {
    return editedAt;
  }

  public void setEditedAt(Instant editedAt) {
    this.editedAt = editedAt;
  }

  public User getEditedBy() {
    return editedBy;
  }

  public void setEditedBy(User editedBy) {
    this.editedBy = editedBy;
  }

  public int getVotes() {
    return votes;
  }

  public void setVotes(int votes) {
    this.votes = votes;
  }
}
