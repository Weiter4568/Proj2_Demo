package cse.java2.project.repository;

import cse.java2.project.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
  @Query("SELECT COUNT(q), q.userID FROM Question q GROUP BY q.userID")
  List<Object[]> countQuestionsPerUser();
}
