package com.example.demo.controllers;

import com.example.demo.models.Team;
import com.example.demo.models.User;
import com.example.demo.repositories.TeamRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TeamRepository teamRepository;

    // Endpoint to create a new user
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return new ResponseEntity<>("Username already exists", HttpStatus.CONFLICT);
        }

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }

        if (user.getRole().equals("ROLE_ADMIN")) {
            // Only admins can be assigned to a team, and this is where we associate them with a team
            Team team = teamRepository.findByName(user.getTeam().getName())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            user.setTeam(team);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // Endpoint to get the role of the logged-in user
    @GetMapping("/role")
    public ResponseEntity<?> getUserRole(Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByUsername(principal.getName());
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Map<String, String> response = new HashMap<>();
        response.put("role", user.getRole());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}


