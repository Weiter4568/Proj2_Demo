package cse.java2.project.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    private Long id;

    private String displayName;
    private String location;
    private Integer reputation;

    // getters and setters
}
