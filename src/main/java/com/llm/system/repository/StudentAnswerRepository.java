package com.llm.system.repository;

import com.llm.system.entity.Question;
import com.llm.system.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM StudentAnswer sa WHERE sa.question = :question")
    void deleteByQuestion(Question question);
}
