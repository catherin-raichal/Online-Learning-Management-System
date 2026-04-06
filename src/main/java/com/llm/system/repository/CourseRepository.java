package com.llm.system.repository;

import com.llm.system.entity.Course;
import com.llm.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacher(User teacher);

    List<Course> findByStudentsContaining(User student);
}
