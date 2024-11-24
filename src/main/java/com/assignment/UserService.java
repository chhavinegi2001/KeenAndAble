package com.assignment;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    // ExecutorService for handling parallel tasks
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Adjustable pool size

    // Thread-safe map for temporary password storage
    private final ConcurrentHashMap<String, String> passwordMap = new ConcurrentHashMap<>();

  
    // Set to track deleted usernames
    private final Set<String> deletedUsernames = Collections.synchronizedSet(new HashSet<>());

    @Autowired
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

  
    // Bulk user registration with password generation and encryption (supports single and multiple users)
    public List<UserRegistrationResponse> bulkRegisterUsers(List<UserRegistrationRequest> userRequests) {
        if (userRequests == null || userRequests.isEmpty()) {
            logger.error("User registration request is empty");
            throw new IllegalArgumentException("User registration request cannot be empty");
        }

        logger.info("Starting user registration for {} users", userRequests.size());

        // List to store responses
        List<UserRegistrationResponse> registrationResponses = Collections.synchronizedList(new ArrayList<>());

        // CountDownLatch to ensure all threads finish processing
        CountDownLatch latch = new CountDownLatch(userRequests.size());

        for (UserRegistrationRequest userRequest : userRequests) {
            executorService.submit(() -> {
                try {
                    logger.debug("Processing registration for user: {}", userRequest.getUserName());

                    // Check if the username exists (including soft-deleted users)
                    Optional<User> existingUser = userRepository.findByUserName(userRequest.getUserName());
                    if (existingUser.isPresent() && !existingUser.get().isDeleted()) {
                        String message = "Username '" + userRequest.getUserName() + "' already exists and cannot be reused.";
                        logger.warn(message);
                        registrationResponses.add(new UserRegistrationResponse(
                                userRequest.getUserName(), "Failed", message));
                        return;
                    }

                    // Generate and encrypt password
                    String generatedPassword = generateRandomPassword();
                    String encryptedPassword = passwordEncoder.encode(generatedPassword);

                    // Create and save user
                    User user = new User();
                    user.setUserName(userRequest.getUserName());
                    user.setEncryptedPassword(encryptedPassword);
                    user.setDeleted(false); // Ensure user is active
                    user.setGeneratedPassword(generatedPassword);

                    User savedUser = userRepository.save(user);

                    // Add success response
                    registrationResponses.add(new UserRegistrationResponse(
                            savedUser.getUserName(), "Success", "User registered successfully."));

                    logger.info("User '{}' registered successfully.", savedUser.getUserName());
                } catch (Exception e) {
                    String message = "Unexpected error occurred while registering user: " + e.getMessage();
                    logger.error(message, e);
                    registrationResponses.add(new UserRegistrationResponse(
                            userRequest.getUserName(), "Failed", message));
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            // Wait for all threads to complete
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Bulk registration process interrupted", e);
            Thread.currentThread().interrupt();
        }

        logger.info("User registration process completed.");
        return registrationResponses;
    }

    // Method to generate a random password
    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) { // Password length 8
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }
 

 // Wrapper method to handle single user registration
    public UserRegistrationResponse registerSingleUser(UserRegistrationRequest userRequest) {
        logger.info("Registering single user: {}", userRequest.getUserName());

        // Reuse the bulk logic for a single user
        List<UserRegistrationResponse> responses = bulkRegisterUsers(Collections.singletonList(userRequest));

        // Return the first (and only) response
        return responses.get(0);
    }
 // Delete a user and track username
    public void deleteUser(String userName) {
        logger.info("Attempting to delete user: {}", userName);

        // Find user, including soft-deleted ones
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", userName);
                    return new IllegalArgumentException("User not found");
                });

        if (user.isDeleted()) {
            logger.warn("User '{}' is already marked as deleted.", userName);
            throw new IllegalArgumentException("User is already deleted");
        }

        // Mark the user as deleted
        user.setDeleted(true);
        userRepository.save(user);

        logger.info("User '{}' deleted successfully and cannot be reused.", userName);
        throw new IllegalArgumentException("deleted successfully");
        
    }


    // Update a user's username
    public User updateUser(String userName, String newUserName) {
        logger.info("Attempting to update username from '{}' to '{}'", userName, newUserName);

        if (deletedUsernames.contains(newUserName) || userRepository.existsByUserName(newUserName)) {
            throw new IllegalArgumentException("New username is unavailable.");
        }

        User user = userRepository.findByUserNameAndIsDeletedFalse(userName)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", userName);
                    return new IllegalArgumentException("User not found");
                });

        user.setUserName(newUserName);
        User updatedUser = userRepository.save(user);

        logger.info("User updated successfully: {}", newUserName);
        return updatedUser;
    }

 // Retrieve a user by password
    public User getUserByPassword(String password) {
        logger.info("Starting to retrieve user by password.");

        List<User> users = userRepository.findAll();
        for (User user : users) {
            // Check if the user is deleted, if so, skip this user
            if (user.isDeleted()) {
                logger.warn("User '{}' is deleted and cannot be retrieved by password.", user.getUserName());
                continue;  // Skip this user if deleted
            }
            
            if (passwordEncoder.matches(password, user.getEncryptedPassword())) {
                logger.info("Password match found for user: {}", user.getUserName());
                return user;
            }
        }

        logger.warn("No user found matching the provided password.");
        return null;
    }

 // Parse file for bulk registration
    public List<UserRegistrationRequest> parseFile(MultipartFile file) throws IOException {
        List<UserRegistrationRequest> userRequests = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 2) {
                    String userName = data[0];
                    String password = data[1];
                    userRequests.add(new UserRegistrationRequest(userName, password));
                }
            }
        }

        return userRequests;
    }


    // Get all active users
    public List<User> getAllUsers() {
        logger.info("Fetching all active users");
        return userRepository.findAll().stream()
                .filter(user -> !user.isDeleted())
                .collect(Collectors.toList());
    }
}