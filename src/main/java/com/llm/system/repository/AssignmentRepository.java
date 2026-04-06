package com.llm.system.repository;

import com.llm.system.entity.Assignment;
import com.llm.system.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseIn(List<Course> courses);
}
