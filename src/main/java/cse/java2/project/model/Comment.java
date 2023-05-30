package cse.java2.project.model;

import javax.persistence.*;

@Entity
@Table(name = "comments")
public class Comment {
  @Id
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "post_id")
  private Integer postId;

  @Column(name = "score")
  private Integer score;

  @Column(name = "user_id")
  private Integer userId;

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  public Integer getPostId() {
    return postId;
  }

  public void setPostId(Integer postId) {
    this.postId = postId;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }
}
