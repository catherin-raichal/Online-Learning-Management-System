package com.llm.system.repository;

import com.llm.system.entity.StudyMaterial;
import com.llm.system.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {
    List<StudyMaterial> findByCourse(Course course);
}
