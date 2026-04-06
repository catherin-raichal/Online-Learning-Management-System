package com.llm.system.repository;

import com.llm.system.entity.Assignment;
import com.llm.system.entity.Submission;
import com.llm.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByStudent(User student);

    List<Submission> findByAssignmentIn(List<Assignment> assignments);
}
