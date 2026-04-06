package com.llm.system.config;

import com.llm.system.entity.Course;
import com.llm.system.entity.User;
import com.llm.system.repository.CourseRepository;
import com.llm.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData(UserRepository userRepo, CourseRepository courseRepo) {
        return args -> {
            // 1. Create or Update Teacher
            User teacherUser = userRepo.findByUsername("teacher").orElse(new User());
            teacherUser.setUsername("teacher");
            teacherUser.setEmail("teacher@ots.com");
            teacherUser.setPassword(passwordEncoder.encode("password"));
            teacherUser.setRole("TEACHER");
            userRepo.save(teacherUser);
            System.out.println("Data Seeder: Teacher user updated/created.");

            // 2. Create or Update Student
            User studentUser = userRepo.findByUsername("student").orElse(new User());
            studentUser.setUsername("student");
            studentUser.setEmail("student@ots.com");
            studentUser.setPassword(passwordEncoder.encode("password"));
            studentUser.setRole("STUDENT");
            userRepo.save(studentUser);
            System.out.println("Data Seeder: Student user updated/created.");

            // 3. Create Admin
            User adminUser = userRepo.findByUsername("admin").orElse(new User());
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@ots.com");
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setRole("ADMIN");
            userRepo.save(adminUser);
            System.out.println("Data Seeder: Admin user updated/created.");

            // 3. Create Course
            if (courseRepo.count() == 0) {
                User teacher = userRepo.findByUsername("teacher").get();
                Course course = new Course();
                course.setName("Java 101");
                course.setDescription("Introduction to Java Programming");
                course.setTeacher(teacher);
                courseRepo.save(course);
                System.out.println("Data Seeder: Created default course 'Java 101'");
            }
        };
    }
}
