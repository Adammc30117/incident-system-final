package com.example.demo.controllers;

import com.example.demo.models.Team;
import com.example.demo.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    // ✅ Create a new team (delegating logic to TeamService)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createTeam(@RequestBody Team team) {
        String response = teamService.createTeam(team);

        return response.equals("Team created successfully!")
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
