package com.example.demo.controllers;

import com.example.demo.models.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * REST controller responsible for managing user operations such as user creation
 * and retrieving roles of authenticated users.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Creates a new user.
     *
     * @param user User details provided in the request body.
     * @return ResponseEntity containing created user details or an error message.
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        String response = userService.createUser(user);

        if (response.equals("User created successfully!")) {
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    /**
     * Retrieves the role of the currently authenticated user.
     *
     * @param principal Currently authenticated user.
     * @return ResponseEntity containing the user's role or an appropriate error message.
     */
    @GetMapping("/role")
    public ResponseEntity<?> getUserRole(Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String role = userService.getUserRole(principal.getName());

        if (role.equals("User not found")) {
            return new ResponseEntity<>(role, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(Map.of("role", role), HttpStatus.OK);
    }
}
