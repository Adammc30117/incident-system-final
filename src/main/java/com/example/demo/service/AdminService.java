package com.example.demo.service;

import com.example.demo.models.User;
import com.example.demo.models.Team;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;  // Inject TeamRepository

    public List<User> getAllAdmins() {
        return userRepository.findByRole("ROLE_ADMIN");
    }

    public List<User> getAdminsByTeam(Long teamId) {
        return userRepository.findByRoleAndTeamId("ROLE_ADMIN", teamId);
    }

    public User createAdmin(String username, String password, Long teamId) {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Admin already exists!");
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(password);  // Encode this properly in a real system
        admin.setRole("ROLE_ADMIN");
        admin.setTeam(teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found")));

        return userRepository.save(admin);
    }
}
