package com.example.demo.controllers;

import com.example.demo.models.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // ✅ Create a new user (delegating logic to UserService)
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        String response = userService.createUser(user);

        if (response.equals("User created successfully!")) {
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    // ✅ Get the role of the logged-in user (delegating logic to UserService)
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
