package com.example.demo.service;

import com.example.demo.models.Team;
import com.example.demo.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    // ✅ Create a new Team
    public String createTeam(Team team) {
        Optional<Team> existingTeam = teamRepository.findByName(team.getName());
        if (existingTeam.isPresent()) {
            return "Team name already exists!";
        }

        teamRepository.save(team);
        return "Team created successfully!";
    }
}
