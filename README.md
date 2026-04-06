# Online Learning Management System

This is a Spring Boot application for managing online task submissions, quizzes, and course materials.

## Prerequisites
- **Java**: JDK 17 or higher (Verified with JDK 24)
- **Database**: MySQL (Ensure your database is running and credentials match `application.properties`)

## How to Run the Project

### 1. Build the Project
Open a terminal in the project root directory and run:

```bash
mvnw clean package -DskipTests
```
*Note: If you are on Windows and `mvnw` is not working, try `.\mvnw.cmd`.*

### 2. Run the Application
After a successful build, a JAR file will be created in the `target` directory. Run it using:

```bash
java -jar target/system-0.0.1-SNAPSHOT.jar
```

Wait until you see `Started SystemApplication in ... seconds`.

### 3. Access the Application
Open your web browser and go to:
[http://localhost:8081](http://localhost:8081)

## Login Credentials
- **Teacher**: `teacher` / `password`
- **Student**: `student` / `password`
- **Admin**: `admin` / `admin`

## Project Structure (Key Files)
- **Controllers**: `src/main/java/com/llm/system/controller/`
    - `DashboardController.java`: Handles dashboard views, assignments, and submissions.
    - `QuizController.java`: Handles quiz creation, questions, and results.
- **Services**: `src/main/java/com/llm/system/service/`
    - `UserService.java`: Manages user registration.
    - `FileStorageService.java`: Manages file uploads.

## Recent Updates
- Simplified code logic (Loops instead of Streams) for better beginner readability.
- Removed "Forgot Password" functionality.
- Added explanatory comments to key methods.
