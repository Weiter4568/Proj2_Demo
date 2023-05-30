package cse.java2.project.repository;

import cse.java2.project.model.Codeblock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeBlockRepository extends JpaRepository<Codeblock, Long> {
}
