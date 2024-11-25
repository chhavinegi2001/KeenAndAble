package com.assignment;



import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

	 private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	    @Autowired
	    private UserService userService;
	    @Autowired
	    private
	    UserRepository userRepository;

// it can register in bulk and as well as single by ensuring the uniqueness or generating encrypt password
	    @PostMapping("/register")
	    public List<UserRegistrationResponse> registerUsers(@RequestBody List<UserRegistrationRequest> userRequests) {
	        logger.info("Received bulk registration request for {} users", userRequests.size());
	        List<UserRegistrationResponse> responses = userService.bulkRegisterUsers(userRequests);
	        logger.info("Bulk registration completed with {} results", responses.size());
	        return responses;
	    }

//it will soft delete the data by setting the flag true for username can not be reused in future
	    @DeleteMapping("/{userName}")
	    public ResponseEntity<String> deleteUser(@PathVariable String userName) {
	        logger.info("Received delete request for user: {}", userName);
	        
	        try {
	            userService.deleteUser(userName);
	            logger.info("User deleted successfully: {}", userName);
	            return ResponseEntity.ok("User deleted successfully");
	        } catch (IllegalArgumentException e) {
	            logger.error("Error during user deletion: {}", e.getMessage());
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	        }
	    }

//for updating the username and any details of user we have in future 
	    @PutMapping("/{userName}")
	    public User updateUser(@PathVariable String userName, @RequestParam String newUserName) {
	        logger.info("Received update request for user: {} to {}", userName, newUserName);
	        User updatedUser = userService.updateUser(userName, newUserName);
	        logger.info("User updated to: {}", newUserName);
	        return updatedUser;
	    }
// by this we can get the username via generatedPassword that is linked with username and saved in db as encypted password 
	    @GetMapping("/getUserByPassword")
	    public ResponseEntity<?> getUserByPassword(@RequestParam("password") String password) {
	        logger.info("Received request to fetch user by password.");

	        User user = userService.getUserByPassword(password);

	        if (user != null) {
	            UserResponseDTO response = new UserResponseDTO(user.getUserName());
	            return ResponseEntity.ok(response);
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                    .body("User is deleted or no match found");
	        }
	    }

// file format should be like  
	    //user1
	  //  user2
	  //  user3
//in csv format
	    @PostMapping("/uploadFile")
	    public ResponseEntity<String> bulkRegisterUsersFromFile(@RequestParam("file") MultipartFile file) {
	        try {
	            // Parse the file to get user registration requests using the service
	            List<UserRegistrationRequest> userRequests = userService.parseFile(file);

	            // Bulk register users using the service
	            List<UserRegistrationResponse> registrationResponses = userService.bulkRegisterUsers(userRequests);

	            // Build response messages
	            List<String> successMessages = registrationResponses.stream()
	                    .filter(response -> "Success".equals(response.getStatus()))
	                    .map(UserRegistrationResponse::getMessage)
	                    .collect(Collectors.toList());

	            List<String> failureMessages = registrationResponses.stream()
	                    .filter(response -> "Failure".equals(response.getStatus()))
	                    .map(UserRegistrationResponse::getMessage)
	                    .collect(Collectors.toList());

	            // Return the appropriate response
	            String responseMessage = String.format("Success: %d, Failures: %d", successMessages.size(), failureMessages.size());
	            return ResponseEntity.ok(responseMessage);

	        } catch (Exception e) {
	            logger.error("Error processing file", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
	        }
	    }

//get api for getting all the users
	    @GetMapping("/allUsers")
	    public List<User> getAllUsers() {
	        logger.info("Received request to fetch all users");
	        List<User> users = userService.getAllUsers();
	        logger.info("Returning {} users", users.size());
	        return users;
	    }
	}
