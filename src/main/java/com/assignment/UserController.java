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
	    public void deleteUser(@PathVariable String userName) {
	        logger.info("Received delete request for user: {}", userName);
	        userService.deleteUser(userName);
	        logger.info("User deleted: {}", userName);
	    }
//for updatng the username and any details of user we have in future 
	    @PutMapping("/{userName}")
	    public User updateUser(@PathVariable String userName, @RequestParam String newUserName) {
	        logger.info("Received update request for user: {} to {}", userName, newUserName);
	        User updatedUser = userService.updateUser(userName, newUserName);
	        logger.info("User updated to: {}", newUserName);
	        return updatedUser;
	    }
// by this we can get the username via generatedPassword that is linked with username and saved in db as encypted password 
	    @GetMapping("/getUserByPassword/{password}")
	    public ResponseEntity<?> getUserByPassword(@PathVariable String password) {
	        User user = userService.getUserByPassword(password);
	        
	        if (user != null) {
	            return ResponseEntity.ok(user);  // Return user if found
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User is deleted or no match found");  // Return 404 if user is deleted or not found
	        }
	    }
//consumes csv files for bulk upload and csv have username,password and it can be multiple
	    @PostMapping("/uploadFile")
	    public ResponseEntity<String> bulkRegisterUsersFromFile(@RequestParam("file") MultipartFile file) {
	        try {
	            // Parse the file to get user registration requests
	            List<UserRegistrationRequest> userRequests = userService.parseFile(file);
	            List<String> existingUsers = new ArrayList<>();
	            List<UserRegistrationResponse> registrationResponses = new ArrayList<>();

	            for (UserRegistrationRequest userRequest : userRequests) {
	                Optional<User> existingUser = userRepository.findByUserName(userRequest.getUserName());

	                if (existingUser.isPresent()) {
	                    existingUsers.add(userRequest.getUserName());
	                } else {
	                    // Register the user and store the response
	                    UserRegistrationResponse response = userService.registerSingleUser(userRequest);
	                    registrationResponses.add(response);
	                }
	            }

	            // Prepare the response message
	            if (!existingUsers.isEmpty()) {
	                String existingUsersMessage = "The following users already exist: " + String.join(", ", existingUsers);
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(existingUsersMessage);
	            } else {
	                return ResponseEntity.ok("Bulk users registered successfully.");
	            }

	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file");
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
