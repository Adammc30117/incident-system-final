package com.example.demo.service;

import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AdminService.java
 *
 * This service provides functionality for managing administrative users.
 * It supports retrieving admins, filtering by team, and creating new admins.
 */

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    /**
     * Returns a list of all users with the role 'ROLE_ADMIN'.
     */
    public List<User> getAllAdmins() {
        return userRepository.findByRole("ROLE_ADMIN");
    }

    /**
     * Returns a list of admins assigned to a specific team.
     *
     * @param teamId the ID of the team
     * @return list of users with 'ROLE_ADMIN' and the given team ID
     */
    public List<User> getAdminsByTeam(Long teamId) {
        return userRepository.findByRoleAndTeamId("ROLE_ADMIN", teamId);
    }

    /**
     * Creates a new admin user and assigns them to a team.
     *
     * @param username the new admin's username
     * @param password the new admin's password (plaintext, should be encoded in a real system)
     * @param teamId   the team ID to assign
     * @return the created User entity
     */
    public User createAdmin(String username, String password, Long teamId) {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Admin already exists!");
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(password);  // Note: encode this in production
        admin.setRole("ROLE_ADMIN");
        admin.setTeam(teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found")));

        return userRepository.save(admin);
    }
}
