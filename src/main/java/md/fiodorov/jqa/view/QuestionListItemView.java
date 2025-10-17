package md.fiodorov.jqa.view;

import java.time.Instant;

public class QuestionListItemView {

  private Long id;
  private String title;
  private String content;
  private String author;
  private String rank;
  private Instant createdDate;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getRank() {
    return rank;
  }

  public void setRank(String rank) {
    this.rank = rank;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Instant createdDate) {
    this.createdDate = createdDate;
  }
}
