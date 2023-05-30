package cse.java2.project.model;

import javax.persistence.*;

@Entity
@Table(name = "questions")
public class Question {
  @Id
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "answered")
  private Boolean answered;

  @Column(name = "answer_count")
  private Integer answerCount;

  @Column(name = "title")
  private String title;

  @Column(name = "creation_date")
  private Integer creationDate;

  @Column(name = "tags")
  private String tags;

  @Column(name = "user_id")
  private Integer userID;

  @Column(name = "score")
  private Integer score;

  @Column(name = "view_cnt")
  private Integer viewCnt;

  public Integer getViewCnt() {
    return viewCnt;
  }

  public void setViewCnt(Integer viewCnt) {
    this.viewCnt = viewCnt;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public Integer getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Integer creationDate) {
    this.creationDate = creationDate;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Integer getAnswerCount() {
    return answerCount;
  }

  public void setAnswerCount(Integer answerCount) {
    this.answerCount = answerCount;
  }

  public Boolean getAnswered() {
    return answered;
  }

  public void setAnswered(Boolean answered) {
    this.answered = answered;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getUserID() {
    return userID;
  }

  public void setUserID(Integer userID) {
    this.userID = userID;
  }

  @Override
  public String toString() {
    return "Question{"
        + "id="
        + id
        + ", answered="
        + answered
        + ", answerCount="
        + answerCount
        + ", title='"
        + title
        + '\''
        + ", creationDate="
        + creationDate
        + ", tags='"
        + tags
        + '\''
        + ", userID="
        + userID
        + '}';
  }
}
