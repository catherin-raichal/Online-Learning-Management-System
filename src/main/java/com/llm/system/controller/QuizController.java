package com.llm.system.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.llm.system.entity.Course;
import com.llm.system.entity.Option;
import com.llm.system.entity.Question;
import com.llm.system.entity.Quiz;
import com.llm.system.entity.QuizResult;
import com.llm.system.entity.StudentAnswer;
import com.llm.system.entity.User;
import com.llm.system.repository.CourseRepository;
import com.llm.system.repository.OptionRepository;
import com.llm.system.repository.QuestionRepository;
import com.llm.system.repository.QuizRepository;
import com.llm.system.repository.QuizResultRepository;
import com.llm.system.repository.StudentAnswerRepository;
import com.llm.system.repository.UserRepository;

// This class handles all Quiz-related actions (creation, taking, grading)
@Controller
public class QuizController {

    // Access to Quiz data
    @Autowired private QuizRepository quizRepository;
    
    // Access to Course data
    @Autowired private CourseRepository courseRepository;
    
    // Access to Question data
    @Autowired private QuestionRepository questionRepository;
    
    // Access to Quiz Results (scores)
    @Autowired private QuizResultRepository quizResultRepository;
    
    // Access to User data
    @Autowired private UserRepository userRepository;
    
    // Access to Student Answers (individual question responses)
    @Autowired private StudentAnswerRepository studentAnswerRepository;
    
    // Access to Options (A, B, C, D choices)
    @Autowired private OptionRepository optionRepository;

    // --------------------------------------------------------------------------------
    // TEACHER ACTIONS
    // --------------------------------------------------------------------------------

    // Step 1: Teacher clicks "Create Quiz" -> Show the form
    @GetMapping("/teacher/create-quiz")
    public String showCreateQuizForm(@RequestParam("courseId") Long courseId, Model model) {
        // Find the course this quiz will belong to
        Course course = courseRepository.findById(courseId).orElseThrow();
        
        // Send the course info to the webpage
        model.addAttribute("course", course);
        
        // Open the 'create-quiz.jsp' page
        return "create-quiz";
    }

    // Step 2: Teacher submits the "Create Quiz" form
    @PostMapping("/teacher/create-quiz")
    public String createQuiz(@RequestParam("courseId") Long courseId,
                             @RequestParam("title") String title,
                             @RequestParam("timeLimit") Integer timeLimit) {
                             
        // Find the course again to link it
        Course course = courseRepository.findById(courseId).orElseThrow();
        
        // Create a new empty Quiz object
        Quiz quiz = new Quiz();
        
        // Set the title (e.g., "Midterm Exam")
        quiz.setTitle(title);
        
        // Set the time limit (e.g., 30 minutes)
        quiz.setTimeLimitMinutes(timeLimit);
        
        // Link the quiz to the course
        quiz.setCourse(course);
        
        // Save the new quiz to the database
        quiz = quizRepository.save(quiz);

        // Redirect the teacher to the "View Quiz" page to add questions
        return "redirect:/teacher/view-quiz?quizId=" + quiz.getId();
    }
    
    // Step 3: Teacher views the quiz (to add/remove questions)
    @GetMapping("/teacher/view-quiz")
    public String viewQuiz(@RequestParam("quizId") Long quizId, Model model) {
        // Find the quiz by its ID
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        
        // Add the quiz (and its existing questions) to the model
        model.addAttribute("quiz", quiz);
        
        // Show the 'view-quiz.jsp' page
        return "view-quiz";
    }
    
    // Step 4: Teacher adds a new question to the quiz
    @PostMapping("/teacher/add-question")
    public String addQuestion(@RequestParam("quizId") Long quizId,
                              @RequestParam("text") String text,
                              @RequestParam("option1") String opt1,  // Choice A
                              @RequestParam(value = "correct1", required = false) boolean correct1,
                              @RequestParam("option2") String opt2,  // Choice B
                              @RequestParam(value = "correct2", required = false) boolean correct2,
                              @RequestParam("option3") String opt3,  // Choice C
                              @RequestParam(value = "correct3", required = false) boolean correct3,
                              @RequestParam("option4") String opt4,  // Choice D
                              @RequestParam(value = "correct4", required = false) boolean correct4) {
                              
        // Find the quiz we are adding to
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        
        // Create a new Question object
        Question question = new Question();
        question.setText(text); // e.g., "What is 2+2?"
        question.setQuiz(quiz); // Link it to the quiz
        
        // Create a list to hold the 4 options
        List<Option> options = new ArrayList<>();
        
        // Helper method to create option objects (see below)
        options.add(createOption(opt1, correct1, question));
        options.add(createOption(opt2, correct2, question));
        options.add(createOption(opt3, correct3, question));
        options.add(createOption(opt4, correct4, question));
        
        // Add the options to the question
        question.setOptions(options);
        
        // Save the question (and options automatically) to the database
        questionRepository.save(question); 
        
        // Return to the quiz view so they can see the new question
        return "redirect:/teacher/view-quiz?quizId=" + quizId;
    }

    // Helper method to make code cleaner
    private Option createOption(String text, boolean isCorrect, Question question) {
        Option o = new Option();
        o.setText(text);
        o.setCorrect(isCorrect);
        o.setQuestion(question);
        return o;
    }

    // Step 5: Teacher checks how students are doing
    @GetMapping("/teacher/quiz-results")
    public String viewQuizResults(@RequestParam("quizId") Long quizId, Model model) {
        // Find the quiz
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        
        // Find everybody who has submitted this quiz
        List<QuizResult> submittedResults = quizResultRepository.findByQuiz(quiz);
        
        // Now, let's find who hasn't submitted yet
        // First, get all students in the course
        Course course = quiz.getCourse();
        List<User> allStudents = course.getStudents();
        
        // Make a list of IDs of students who submitted
        List<Long> submittedStudentIds = new ArrayList<>();
        for (QuizResult result : submittedResults) {
            submittedStudentIds.add(result.getStudent().getId());
        }
        
        // Check "All Students" against "Submitted ID List" to find the pending ones
        List<User> pendingStudents = new ArrayList<>();
        for (User student : allStudents) {
            if (!submittedStudentIds.contains(student.getId())) {
                pendingStudents.add(student);
            }
        }
        
        // Send all lists to the webpage
        model.addAttribute("quiz", quiz);
        model.addAttribute("results", submittedResults);
        model.addAttribute("pendingStudents", pendingStudents);
        
        return "teacher-quiz-results";
    }

    // Step 6: Teacher wants to delete a question
    @PostMapping("/teacher/delete-question")
    public String deleteQuestion(@RequestParam("questionId") Long questionId, @RequestParam("quizId") Long quizId) {
        // Find the question, or fail if not found
        Question question = questionRepository.findById(questionId).orElseThrow();
        
        // CAUTION: We must delete all student answers to this question first!
        // If we don't, the database will block us (Foreign Key Error).
        studentAnswerRepository.deleteByQuestion(question);
        
        // Remove the question from the quiz object in memory
        Quiz quiz = question.getQuiz();
        quiz.getQuestions().remove(question);
        
        // Save the quiz to update the relationship
        quizRepository.save(quiz);
        
        // Now delete the question itself from the database
        questionRepository.delete(question);
        
        // Go back to the quiz view
        return "redirect:/teacher/view-quiz?quizId=" + quizId;
    }

    // Step 7: Teacher wants to delete the WHOLE quiz
    @PostMapping("/teacher/delete-quiz")
    public String deleteQuiz(@RequestParam("quizId") Long quizId) {
        // Find the quiz
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        
        // Find all results (grades) associated with this quiz
        List<QuizResult> results = quizResultRepository.findByQuiz(quiz);
        
        // Delete all those results first
        if (results != null && !results.isEmpty()) {
            quizResultRepository.deleteAll(results);
        }
        
        // Now delete the quiz. 
        // Note: Questions will be deleted automatically if "Cascade" is set, 
        // but often it's safer to rely on the database or clean them up too.
        // In our case, JPA handles the cascade for questions.
        quizRepository.delete(quiz);
        
        // Go back to the main dashboard
        return "redirect:/dashboard";
    }

    // --------------------------------------------------------------------------------
    // STUDENT ACTIONS
    // --------------------------------------------------------------------------------

    // Step 1: Student clicks "Take Quiz"
    @GetMapping("/student/take-quiz")
    public String takeQuiz(@RequestParam("quizId") Long quizId, Model model) {
        // Find the quiz
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        
        // Send it to the page so questions can be displayed
        model.addAttribute("quiz", quiz);
        
        // Show the 'take-quiz.jsp' page
        return "take-quiz";
    }

    // Step 2: Student submits their answers
    @PostMapping("/student/submit-quiz")
    public String submitQuiz(@RequestParam("quizId") Long quizId,
                             @RequestParam Map<String, String> requestParams, // Contains all selected answers
                             Principal principal,
                             Model model) {
                             
        // Find the student (User) who is logged in
        User student = userRepository.findByUsername(principal.getName()).orElseThrow();
        
        // Find the quiz they just took
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        
        // Initialize score counter
        int score = 0;
        
        // Initialize total questions counter
        int totalQuestions = quiz.getQuestions().size();
        
        // Create the generic Result object (Score: 0 for now)
        QuizResult result = new QuizResult();
        result.setStudent(student);
        result.setQuiz(quiz);
        result.setTotalQuestions(totalQuestions);
        result.setSubmissionTime(LocalDateTime.now());
        
        // Save it so we get an ID
        QuizResult savedResult = quizResultRepository.save(result);
        
        // Loop through every question in the quiz to check answers
        for (Question question : quiz.getQuestions()) {
            // The form sends answers as "question_123" -> "456" (where 123 is Q-ID, 456 is Option-ID)
            String selectedOptionIdStr = requestParams.get("question_" + question.getId());
            
            // Create an answer record
            StudentAnswer studentAnswer = new StudentAnswer();
            studentAnswer.setQuizResult(savedResult);
            studentAnswer.setQuestion(question);
            
            // If the student actually selected something...
            if (selectedOptionIdStr != null) {
                try {
                	// Convert the string ID to a Long ID
                	Long selectedOptionId = Long.parseLong(selectedOptionIdStr);
                	
                	// Find the full Option object from database
                	Option selectedOption = optionRepository.findById(selectedOptionId).orElse(null);
                
                	if (selectedOption != null) {
                	    // Record their choice
                    	studentAnswer.setSelectedOption(selectedOption);
                    	
                    	// Check if it was the RIGHT choice
                    	if (selectedOption.isCorrect()) {
                    	    // If yes, increase score!
                        	score++;
                    	}
                	}
                } catch (NumberFormatException e) {
                	// Ignore invalid data
                }
            }
            // Save this individual answer to the database
            studentAnswerRepository.save(studentAnswer);
        }
        
        // Update the final score on the Result object
        savedResult.setScore(score);
        
        // Save the final result
        quizResultRepository.save(savedResult);
        
        // Show the result page
        return "redirect:/student/quiz-result?resultId=" + savedResult.getId();
    }

    // Step 3: Student views their detailed result
    @GetMapping("/student/quiz-result")
    public String quizResult(@RequestParam("resultId") Long resultId, Model model) {
        // Find the result by ID
        QuizResult result = quizResultRepository.findById(resultId).orElseThrow();
        
        // Send it to the page
        model.addAttribute("result", result);
        
        // Show 'quiz-result.jsp'
        return "quiz-result";
    }

    // Step 8: Teacher wants to edit a question - Show the form
    @GetMapping("/teacher/edit-question")
    public String showEditQuestionForm(@RequestParam("questionId") Long questionId, Model model) {
        // Find the question
        Question question = questionRepository.findById(questionId).orElseThrow();
        
        // Add it to the model
        model.addAttribute("question", question);
        
        // Show the 'edit-question.html' page
        return "edit-question";
    }

    // Step 9: Teacher submits the "Edit Question" form
    @PostMapping("/teacher/edit-question")
    public String editQuestion(@RequestParam("questionId") Long questionId,
                               @RequestParam("text") String text,
                               @RequestParam("option1") String opt1,
                               @RequestParam(value = "correct1", required = false) boolean correct1,
                               @RequestParam("option2") String opt2,
                               @RequestParam(value = "correct2", required = false) boolean correct2,
                               @RequestParam("option3") String opt3,
                               @RequestParam(value = "correct3", required = false) boolean correct3,
                               @RequestParam("option4") String opt4,
                               @RequestParam(value = "correct4", required = false) boolean correct4) {
                               
        // Find the existing question
        Question question = questionRepository.findById(questionId).orElseThrow();
        
        // Update the question text
        question.setText(text);
        
        // Get the list of existing options (we assume there are 4)
        List<Option> options = question.getOptions();
        
        // Update each option's text and correctness
        if (options.size() >= 1) {
            options.get(0).setText(opt1);
            options.get(0).setCorrect(correct1);
        }
        if (options.size() >= 2) {
            options.get(1).setText(opt2);
            options.get(1).setCorrect(correct2);
        }
        if (options.size() >= 3) {
            options.get(2).setText(opt3);
            options.get(2).setCorrect(correct3);
        }
        if (options.size() >= 4) {
            options.get(3).setText(opt4);
            options.get(3).setCorrect(correct4);
        }
        
        // Save the updated question (cascade will handle options)
        questionRepository.save(question);
        
        // Redirect back to the quiz view
        return "redirect:/teacher/view-quiz?quizId=" + question.getQuiz().getId();
    }
}
