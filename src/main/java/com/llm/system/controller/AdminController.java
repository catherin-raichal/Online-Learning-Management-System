package com.llm.system.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.llm.system.entity.Course;
import com.llm.system.entity.User;
import com.llm.system.repository.CourseRepository;
import com.llm.system.repository.UserRepository;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        List<User> allUsers = userRepository.findAll();
        List<User> students = allUsers.stream().filter(u -> "STUDENT".equals(u.getRole())).collect(Collectors.toList());
        List<User> teachers = allUsers.stream().filter(u -> "TEACHER".equals(u.getRole())).collect(Collectors.toList());
        List<Course> courses = courseRepository.findAll();
        
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);
        model.addAttribute("courses", courses);
        return "admin-dashboard";
    }

    @PostMapping("/admin/enroll")
    public String enrollStudent(@RequestParam("studentId") Long studentId, @RequestParam("courseId") Long courseId) {
        User student = userRepository.findById(studentId).orElseThrow();
        Course course = courseRepository.findById(courseId).orElseThrow();
        
        if (!course.getStudents().contains(student)) {
            course.getStudents().add(student);
            courseRepository.save(course);
        }
        return "redirect:/admin/dashboard?enrolled";
    }

    @PostMapping("/admin/assign-teacher")
    public String assignTeacher(@RequestParam("teacherId") Long teacherId, @RequestParam("courseId") Long courseId) {
        User teacher = userRepository.findById(teacherId).orElseThrow();
        Course course = courseRepository.findById(courseId).orElseThrow();
        
        course.setTeacher(teacher);
        courseRepository.save(course);
        return "redirect:/admin/dashboard?teacherAssigned";
    }

    @Transactional
    @PostMapping("/admin/delete-user")
    public String deleteUser(@RequestParam("userId") Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        
        // Remove from enrolled courses (for students)
        if ("STUDENT".equals(user.getRole())) {
            for (Course course : user.getEnrolledCourses()) {
                course.getStudents().remove(user);
                courseRepository.save(course);
            }
        }
        
        // Clear teacher from courses (for teachers)
        if ("TEACHER".equals(user.getRole())) {
            List<Course> taughtCourses = courseRepository.findByTeacher(user);
            for (Course course : taughtCourses) {
                course.setTeacher(null);
                courseRepository.save(course);
            }
        }

        userRepository.delete(user);
        return "redirect:/admin/dashboard?deleted";
    }

    @GetMapping("/admin/view-enrolled")
    public String viewEnrolledUsers(@RequestParam("courseId") Long courseId, Model model) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        model.addAttribute("course", course);
        model.addAttribute("students", course.getStudents());
        model.addAttribute("teacher", course.getTeacher());
        return "course-enrolled-users";
    }

    @GetMapping("/admin/create-course")
    public String showCreateCourseForm() {
        return "create-course";
    }

    @PostMapping("/admin/create-course")
    public String createCourse(@RequestParam("name") String name, @RequestParam("description") String description) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        courseRepository.save(course);
        return "redirect:/admin/dashboard?courseCreated";
    }

    @GetMapping("/admin/edit-course")
    public String showEditCourseForm(@RequestParam("courseId") Long courseId, Model model) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        model.addAttribute("course", course);
        return "edit-course";
    }

    @PostMapping("/admin/edit-course")
    public String editCourse(@RequestParam("courseId") Long courseId, @RequestParam("name") String name, @RequestParam("description") String description) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        course.setName(name);
        course.setDescription(description);
        courseRepository.save(course);
        return "redirect:/admin/dashboard?courseUpdated";
    }
}
