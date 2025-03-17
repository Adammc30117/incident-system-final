package com.example.demo.service;

import com.example.demo.models.Team;
import com.example.demo.models.User;
import com.example.demo.repositories.TeamRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TeamRepository teamRepository;

    // ✅ Create a new user
    public String createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return "Username already exists";
        }

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }

        if (user.getRole().equals("ROLE_ADMIN")) {
            // Only admins can be assigned to a team
            Team team = teamRepository.findByName(user.getTeam().getName())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            user.setTeam(team);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User created successfully!";
    }

    // ✅ Get the role of the logged-in user
    public String getUserRole(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return "User not found";
        }
        return user.getRole();
    }
}
