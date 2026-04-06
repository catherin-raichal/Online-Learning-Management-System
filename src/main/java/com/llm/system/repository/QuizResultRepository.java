package com.llm.system.repository;

import com.llm.system.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByStudentId(Long studentId);
    List<QuizResult> findByQuiz(com.llm.system.entity.Quiz quiz);
}
