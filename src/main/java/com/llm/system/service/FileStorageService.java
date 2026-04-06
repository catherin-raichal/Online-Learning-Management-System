package com.llm.system.service;

// Import necessary Java input/output libraries
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// This service is dedicated to saving files that people upload to the website
@Service
public class FileStorageService {

    // Define the folder where files will be saved ("uploads/")
    private final String uploadDir = "uploads/";

    // Constructor: This runs when the application starts
    public FileStorageService() {
        try {
            // Check if the "uploads" folder exists. If not, create it.
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            // If we can't create the folder (e.g., permission error), stop the app
            throw new RuntimeException("Could not create upload directory!");
        }
    }

    // Method to save a file to the disk
    public String storeFile(MultipartFile file) {
        // Step 1: Create a unique filename.
        // We use "UUID.randomUUID()" to generate a random string of characters.
        // This prevents two files with the same name (e.g., "homework.pdf") from overwriting each other.
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        
        try {
            // Step 2: Determine exactly where to put the file on the computer
            // "Paths.get(uploadDir)" gives us the folder
            // ".resolve(fileName)" joins the folder and the filename
            Path targetLocation = Paths.get(uploadDir).resolve(fileName);
            
            // Step 3: Copy the file content to that location
            // "REPLACE_EXISTING" means if a file with this random name somehow exists, replace it
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Step 4: Return the random filename so we can save it in the database
            return fileName;
            
        } catch (IOException ex) {
            // If something goes wrong (disk full, etc.), throw an error
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
}
