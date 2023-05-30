package cse.java2.project.JSON_Model;

import javax.persistence.*;

public class Q3 {
    private String answerRange;
    private Long count;

    public Q3(String answerRange, Long count) {
        this.answerRange = answerRange;
        this.count = count;
    }

    public Q3() {

    }

    public String getAnswerRange() {
        return answerRange;
    }

    public void setAnswerRange(String answerRange) {
        this.answerRange = answerRange;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
