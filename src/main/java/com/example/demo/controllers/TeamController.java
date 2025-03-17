package com.example.demo.controllers;

import com.example.demo.models.Team;
import com.example.demo.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    // Create a new team
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createTeam(@RequestBody Team team) {
        if (teamRepository.findByName(team.getName()).isPresent()) {
            return ResponseEntity.badRequest().body("Team name already exists!");
        }

        teamRepository.save(team); // Save the new team
        return ResponseEntity.ok("Team created successfully!");
    }
}
