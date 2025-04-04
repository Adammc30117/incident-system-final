package com.example.demo.service;

import com.example.demo.models.Team;
import com.example.demo.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * TeamService.java
 *
 * Service responsible for managing teams within the system.
 * Provides business logic for creating teams and validating uniqueness.
 */

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    /**
     * Creates a new team if the name is not already taken.
     *
     * @param team the Team object to be created
     * @return success or error message
     */
    public String createTeam(Team team) {
        Optional<Team> existingTeam = teamRepository.findByName(team.getName());
        if (existingTeam.isPresent()) {
            return "Team name already exists!";
        }

        teamRepository.save(team);
        return "Team created successfully!";
    }
}
