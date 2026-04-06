package com.llm.system.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @ManyToMany
    @JoinTable(name = "enrollments", joinColumns = @JoinColumn(name = "course_id"), inverseJoinColumns = @JoinColumn(name = "student_id"))

    private List<User> students;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public List<User> getStudents() {
        return students;
    }

    public void setStudents(List<User> students) {
        this.students = students;
    }

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)

    private List<Assignment> assignments;

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)

    private List<StudyMaterial> studyMaterials;

    public List<StudyMaterial> getStudyMaterials() {
        return studyMaterials;
    }

    public void setStudyMaterials(List<StudyMaterial> studyMaterials) {
        this.studyMaterials = studyMaterials;
    }

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)

    private List<Quiz> quizzes;

    public List<Quiz> getQuizzes() {
        return quizzes;
    }

    public void setQuizzes(List<Quiz> quizzes) {
        this.quizzes = quizzes;
    }
}
