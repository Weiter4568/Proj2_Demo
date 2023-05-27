package cse.java2.project.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    private Long id;

    private String body;
    private Long postId;
    private Long userId;

    // getters and setters
}

