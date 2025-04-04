package com.example.demo.service;

import com.example.demo.models.Team;
import com.example.demo.models.User;
import com.example.demo.repositories.TeamRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * UserService.java
 *
 * This service handles core logic for user creation and role management.
 * It supports encoding passwords, assigning teams for admin users,
 * and retrieving user roles for authorization purposes.
 */

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TeamRepository teamRepository;

    /**
     * Creates a new user. Assigns default role if not provided,
     * hashes the password, and links the user to a team if they are an admin.
     *
     * @param user the User object to be created
     * @return status message
     */
    public String createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return "Username already exists";
        }

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }

        if (user.getRole().equals("ROLE_ADMIN")) {
            // Only admins should be linked to a team
            Team team = teamRepository.findByName(user.getTeam().getName())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            user.setTeam(team);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User created successfully!";
    }

    /**
     * Retrieves the role for the specified username.
     *
     * @param username the logged-in user's username
     * @return the user's role or a not found message
     */
    public String getUserRole(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "User not found";
        }
        return user.getRole();
    }
}
