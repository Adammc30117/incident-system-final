package com.example.demo.service;

import com.example.demo.models.Incident;
import com.example.demo.models.Team;
import com.example.demo.models.User;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.repositories.TeamRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AssignmentService.java
 *
 * Handles logic for assigning incidents to teams and admins.
 * Ensures proper validation such as verifying team and admin existence
 * and ensuring the admin belongs to the selected team.
 */

@Service
public class AssignmentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Assigns a team and optionally an admin to an incident.
     * Validates the existence of the team and admin, and ensures
     * the admin is part of the selected team.
     *
     * @param incidentId ID of the incident to update
     * @param teamId     ID of the team to assign (nullable for unassignment)
     * @param adminId    ID of the admin to assign (nullable)
     */
    public void assignTeamAndAdmin(Long incidentId, Long teamId, Long adminId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        // Unassign team and admin if teamId is null
        if (teamId == null) {
            incident.setAssignedTeam(null);
            incident.setAssignedAdmin(null);
            incidentRepository.save(incident);
            return;
        }

        // Fetch and assign the team
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found!"));
        incident.setAssignedTeam(team);

        // If no admin selected, just save with the team
        if (adminId == null) {
            incident.setAssignedAdmin(null);
            incidentRepository.save(incident);
            return;
        }

        // Fetch and validate admin
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found!"));

        if (admin.getTeam() == null || !admin.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("Admin does not belong to the selected team!");
        }

        // Assign both team and admin
        incident.setAssignedAdmin(admin);
        incidentRepository.save(incident);
    }
}
