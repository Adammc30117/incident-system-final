package com.example.demo.service;

import com.example.demo.models.Incident;
import com.example.demo.models.Team;
import com.example.demo.models.User;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.repositories.TeamRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    public void assignTeamAndAdmin(Long incidentId, Long teamId, Long adminId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        // If no team is assigned, reset to "Unassigned"
        if (teamId == null) {
            incident.setAssignedTeam(null);
            incident.setAssignedAdmin(null);
            incidentRepository.save(incident);
            return;
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found!"));

        incident.setAssignedTeam(team);

        // If no admin is provided, leave admin as "Unassigned"
        if (adminId == null) {
            incident.setAssignedAdmin(null);
            incidentRepository.save(incident);
            return;
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found!"));

        // Ensure the admin belongs to the assigned team
        if (admin.getTeam() == null || !admin.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("Admin does not belong to the selected team!");
        }


        incident.setAssignedAdmin(admin);
        incidentRepository.save(incident);
    }
}
