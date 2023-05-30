package cse.java2.project.repository;

import cse.java2.project.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query("SELECT COUNT(c), c.userId FROM Comment c GROUP BY c.userId")
  List<Object[]> countCommentsPerUser();
}
