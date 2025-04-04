package com.example.demo.controllers;

import com.example.demo.models.Team;
import com.example.demo.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for managing team-related operations.
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    /**
     * Creates a new team entity.
     *
     * @param team Team details provided in the request body.
     * @return Response indicating success or failure of team creation.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createTeam(@RequestBody Team team) {
        String response = teamService.createTeam(team);

        return response.equals("Team created successfully!")
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
