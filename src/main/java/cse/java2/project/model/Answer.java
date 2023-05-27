package cse.java2.project.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    private Long id;

    private String body;
    private Long questionId;
    private Long userId;

    // getters and setters
}
