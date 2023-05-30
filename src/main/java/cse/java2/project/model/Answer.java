package cse.java2.project.model;

import javax.persistence.*;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "question_id")
    private Integer questionId;

    @Column(name = "is_accepted")
    private Boolean isAccepted;

    @Column(name = "score")
    private Integer score;

    @Column(name = "user_id")
    private Integer userID;

    @Column(name = "accepted_date")
    private Integer acceptedDate;

    public Integer getAcceptedDate() {
        return acceptedDate;
    }

    public void setAcceptedDate(Integer acceptedDate) {
        this.acceptedDate = acceptedDate;
    }

    public void setAccepted(Boolean accepted) {
        isAccepted = accepted;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Boolean getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(Boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Boolean getAccepted() {
        return isAccepted;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", isAccepted=" + isAccepted +
                ", score=" + score +
                ", userID=" + userID +
                '}';
    }
}
