package com.llm.system.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.llm.system.entity.Assignment;
import com.llm.system.entity.Course;
import com.llm.system.entity.StudyMaterial;
import com.llm.system.entity.Submission;
import com.llm.system.entity.User;
import com.llm.system.repository.AssignmentRepository;
import com.llm.system.repository.CourseRepository;
import com.llm.system.repository.StudyMaterialRepository;
import com.llm.system.repository.SubmissionRepository;
import com.llm.system.repository.UserRepository;
import com.llm.system.service.FileStorageService;

// This class handles all the main pages: Dashboard, Assignments, Submissions
@Controller
public class DashboardController {

    // We need access to the User data in the database
    @Autowired
    private UserRepository userRepository;

    // We need access to the Course data
    @Autowired
    private CourseRepository courseRepository;

    // We need access to Assignment data
    @Autowired
    private AssignmentRepository assignmentRepository;

    // We need access to Submission data (student work)
    @Autowired
    private SubmissionRepository submissionRepository;

    // We need this service to help us save uploaded files
    @Autowired
    private FileStorageService fileStorageService;

    // We need access to study materials (PDFs, Videos)
    @Autowired
    private StudyMaterialRepository studyMaterialRepository;

    // This method shows the main dashboard page
    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        // Step 1: Get the username of the person currently logged in
        String username = principal.getName();
        
        // Step 2: Find the full User object from the database using that username
        User user = userRepository.findByUsername(username).get();
        
        // Step 3: Add the user object to the model so the webpage can use it (e.g., show "Welcome, Name")
        model.addAttribute("user", user);

        // Step 4: Check if the user is an ADMIN
        if (user.getRole().equals("ADMIN")) {
            // If admin, send them to the special admin dashboard
            return "redirect:/admin/dashboard";
            
        // Step 5: Check if the user is a TEACHER
        } else if (user.getRole().equals("TEACHER")) {
            // Find all courses created by this teacher
            List<Course> courses = courseRepository.findByTeacher(user);
            
            // Add the list of courses to the model
            model.addAttribute("courses", courses);
            
            // Show the teacher's dashboard page
            return "teacher-dashboard";
            
        // Step 6: Otherwise, the user must be a STUDENT
        } else {
            // Find all courses the student has already joined
            List<Course> enrolledCourses = courseRepository.findByStudentsContaining(user);
            
            // Find ALL courses that exist in the system
            List<Course> allCourses = courseRepository.findAll();
            
            // Remove the ones they are already in, so we only show new ones in "Available Courses"
            allCourses.removeAll(enrolledCourses);

            // Find all assignments for the courses the student is in
            List<Assignment> assignments = assignmentRepository.findByCourseIn(enrolledCourses);
            
            // Find all submissions this student has made
            List<Submission> userSubmissions = submissionRepository.findByStudent(user);
            
            // Send all this data to the webpage
            model.addAttribute("enrolledCourses", enrolledCourses);
            model.addAttribute("availableCourses", allCourses);
            model.addAttribute("assignments", assignments);
            model.addAttribute("submissions", userSubmissions);
            
            // Show the student's dashboard page
            return "student-dashboard";
        }
    }

    // This method allows a student to join a course
    @PostMapping("/student/join-course")
    public String joinCourse(@RequestParam("courseId") Long courseId, Principal principal) {
        // Step 1: Get the currently logged-in student's username
        String username = principal.getName();
        
        // Step 2: Find the student user object
        User student = userRepository.findByUsername(username).get();
        
        // Step 3: Find the course they want to join by its ID
        Course course = courseRepository.findById(courseId).get();
        
        // Step 4: Add the student to the course's list of students
        course.getStudents().add(student);
        
        // Step 5: Save the updated course to the database
        courseRepository.save(course);
        
        // Step 6: Go back to the dashboard
        return "redirect:/dashboard";
    }

    // This method allows a teacher to create a new assignment
    @PostMapping("/teacher/create-assignment")
    public String createAssignment(@RequestParam("courseId") Long courseId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file,
            @RequestParam("deadline") String deadlineStr) {
            
        // Step 1: Find the course this assignment belongs to
        Course course = courseRepository.findById(courseId).get();
        
        // Step 2: Create a new empty Assignment object
        Assignment assignment = new Assignment();
        
        // Step 3: Set the details for the assignment
        assignment.setCourse(course);
        assignment.setTitle(title);
        assignment.setDescription(description);
        
        // Step 4: Save the uploaded file and get its path
        String filePath = fileStorageService.storeFile(file);
        assignment.setFilePath(filePath);
        
        // Step 5: Handle the deadline date
        if (deadlineStr != null && !deadlineStr.isEmpty()) {
            // If the user picked a date, use it
            assignment.setDeadline(LocalDateTime.parse(deadlineStr));
        } else {
            // If they didn't pick a date, set it to 7 days from now automatically
            assignment.setDeadline(LocalDateTime.now().plusDays(7));
        }
        
        // Step 6: Save the new assignment to the database
        assignmentRepository.save(assignment);
        
        // Step 7: Go back to the dashboard
        return "redirect:/dashboard";
    }

    // This method allows a teacher to add study materials (PDFs or Videos)
    @PostMapping("/teacher/add-material")
    public String addStudyMaterial(@RequestParam("courseId") Long courseId,
            @RequestParam("title") String title,
            @RequestParam("type") String type, // "VIDEO" or "PDF"
            @RequestParam(value = "videoUrl", required = false) String videoUrl,
            @RequestParam(value = "file", required = false) MultipartFile file) {
            
        // Step 1: Find the course
        Course course = courseRepository.findById(courseId).get();
        
        // Step 2: Create a new StudyMaterial object
        StudyMaterial material = new StudyMaterial();
        material.setCourse(course);
        material.setTitle(title);
        material.setType(type);

        // Step 3: Check what type of material it is
        if ("VIDEO".equals(type)) {
            // If it's a video, check if it is a YouTube link
            if (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")) {
                // If it is, just save the URL directly
                material.setContent(videoUrl);
            } else {
                // Otherwise, save the URL as is (assumed valid)
                material.setContent(videoUrl);
            }
        } else if ("PDF".equals(type) && file != null && !file.isEmpty()) {
            // If it's a PDF, save the file to our storage
            String storedFileName = fileStorageService.storeFile(file);
            // Save the filename in the database
            material.setContent(storedFileName);
        }

        // Step 4: Save the material to the database
        studyMaterialRepository.save(material);
        
        // Step 5: Go back to dashboard
        return "redirect:/dashboard";
    }

    // This method allows a student to submit their homework
    @PostMapping("/student/submit-task")
    public String submitTask(@RequestParam("assignmentId") Long assignmentId,
            @RequestParam("file") MultipartFile file,
            Principal principal) {
            
        // Step 1: Get the student's username and User object
        String username = principal.getName();
        User student = userRepository.findByUsername(username).get();
        
        // Step 2: Find the assignment they are submitting for
        Assignment assignment = assignmentRepository.findById(assignmentId).get();

        // Step 3: Create a new record for this submission
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        
        // Step 4: Save the student's uploaded file
        String storedFileName = fileStorageService.storeFile(file);
        submission.setFilePath(storedFileName);
        
        // Step 5: Mark the time they submitted it (RIGHT NOW)
        submission.setSubmissionDate(LocalDateTime.now());
        
        // Step 6: Save the submission to the database
        submissionRepository.save(submission);
        
        // Step 7: Go back to dashboard
        return "redirect:/dashboard";
    }

    // This method allows teachers to see who submitted an assignment
    @GetMapping("/teacher/submissions")
    public String viewSubmissions(@RequestParam("assignmentId") Long assignmentId, Model model) {
        // Step 1: Find the assignment
        Assignment assignment = assignmentRepository.findById(assignmentId).get();
        
        // Step 2: Find all submissions for this assignment
        // We use 'List.of(assignment)' because the repository expects a list
        List<Submission> submissions = submissionRepository.findByAssignmentIn(List.of(assignment));
        
        // Step 3: Add data to the model for the webpage
        model.addAttribute("assignment", assignment);
        model.addAttribute("submissions", submissions);
        
        // Step 4: Show the submissions page
        return "view-submissions";
    }

    // This method allows teachers to grade a student's work
    @PostMapping("/teacher/grade")
    public String gradeSubmission(@RequestParam("submissionId") Long submissionId,
            @RequestParam("grade") String grade,
            @RequestParam("feedback") String feedback) {
            
        // Step 1: Find the submission
        Submission submission = submissionRepository.findById(submissionId).get();
        
        // Step 2: Update the grade and feedback
        submission.setGrade(grade);
        submission.setFeedback(feedback);
        
        // Step 3: Save the changes
        submissionRepository.save(submission);
        
        // Step 4: Go back to the submissions list
        return "redirect:/teacher/submissions?assignmentId=" + submission.getAssignment().getId();
    }

    // This method shows which students have NOT yet submitted
    @GetMapping("/teacher/view-pending")
    public String viewPendingSubmissions(@RequestParam("assignmentId") Long assignmentId, Model model) {
        // Step 1: Find the assignment. If it doesn't exist, throw an error.
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow();
        
        // Step 2: Find the course regarding this assignment
        Course course = assignment.getCourse();
        
        // Step 3: Get ALL students enrolled in this course
        List<User> enrolledStudents = course.getStudents();
        
        // Step 4: Get ALL submissions for this assignment
        List<Submission> submissions = submissionRepository.findByAssignmentIn(List.of(assignment));
        
        // Step 5: Make a list of IDs of students who HAVE submitted
        List<Long> submittedStudentIds = new java.util.ArrayList<>();
        for (Submission s : submissions) {
            // Add the student's ID to our list
            submittedStudentIds.add(s.getStudent().getId());
        }
        
        // Step 6: Find students who are enrolled BUT NOT in the submitted list
        List<User> pendingStudents = new java.util.ArrayList<>();
        for (User student : enrolledStudents) {
            // Check if this student's ID is missing from the submitted list
            if (!submittedStudentIds.contains(student.getId())) {
                // If missing, it means they are pending. Add them.
                pendingStudents.add(student);
            }
        }
        
        // Step 7: Send the data to the webpage
        model.addAttribute("assignment", assignment);
        model.addAttribute("pendingStudents", pendingStudents);
        
        // Step 8: Show the pending page
        return "view-pending";
    }

    // This method lets the browser download/display uploaded files
    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // Step 1: Find where the file is on the computer
            Path filePath = Paths.get("uploads/").resolve(filename).normalize();
            
            // Step 2: Turn that path into a resource we can send
            Resource resource = new UrlResource(filePath.toUri());

            // Step 3: Check if the file actually exists
            if (resource.exists()) {
                // Step 4: Guess what kind of file it is (PDF? Image?)
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    // Default to generic file type if we can't guess
                    contentType = "application/octet-stream";
                }

                // Step 5: Send the file back to the browser
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                // If file not found, return 404 error
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // If any error happens, return 500 error
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // This allows a teacher to delete an assignment
    @PostMapping("/teacher/delete-assignment")
    public String deleteAssignment(@RequestParam("assignmentId") Long assignmentId) {
        // Step 1: Find the assignment
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow();
        
        // Step 2: Delete the assignment's PDF file from the valid uploads folder
        try {
            Path filePath = Paths.get("uploads/").resolve(assignment.getFilePath());
            // Try to delete it. If it doesn't exist, just ignore it.
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            // Print error if something goes wrong
            e.printStackTrace();
        }
        
        // Step 3: "Cascade Delete" - Delete ALL submissions for this assignment first
        // If we don't do this, the database will complain about foreign keys
        List<Submission> submissions = submissionRepository.findByAssignmentIn(List.of(assignment));
        for (Submission s : submissions) {
             try {
                // Also delete the file the student uploaded
                Path subPath = Paths.get("uploads/").resolve(s.getFilePath());
                Files.deleteIfExists(subPath);
            } catch (Exception e) {
                // Ignore errors
            }
        }
        
        // Step 4: Delete the submission records from database
        submissionRepository.deleteAll(submissions);
        
        // Step 5: Now it is safe to delete the assignment itself
        assignmentRepository.delete(assignment);
        
        // Step 6: Go back to dashboard
        return "redirect:/dashboard";
    }

    // This allows a teacher to delete study materials
    @PostMapping("/teacher/delete-material")
    public String deleteStudyMaterial(@RequestParam("materialId") Long materialId) {
        // Step 1: Find the material
        StudyMaterial material = studyMaterialRepository.findById(materialId).orElseThrow();
        
        // Step 2: If it is a PDF, delete the file from disk
        if ("PDF".equals(material.getType())) {
             try {
                Path filePath = Paths.get("uploads/").resolve(material.getContent());
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Step 3: Delete the record from database
        try {
            studyMaterialRepository.delete(material);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Step 4: Go back to dashboard
        return "redirect:/dashboard";
    }
}
