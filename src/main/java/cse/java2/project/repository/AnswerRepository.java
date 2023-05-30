package cse.java2.project.repository;

import cse.java2.project.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @Query("SELECT COUNT(a), a.userID FROM Answer a GROUP BY a.userID")
    List<Object[]> countAnswersPerUser();
}