package com.example.demo.service;

import com.example.demo.dto.IncidentMatchResult;
import com.example.demo.models.Incident;
import com.example.demo.models.Team;
import com.example.demo.models.User;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.repositories.TeamRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.search.IncidentSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private IncidentSearchService incidentSearchService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    // ✅ Create a new Incident
    public String createIncident(Incident incident, Principal principal) {
        if (principal == null) {
            return "User not authenticated!";
        }

        incident.setCreatedBy(principal.getName());
        incident.setSeverityLevel(incident.getSeverityLevel() == null ? "Low" : incident.getSeverityLevel());
        incident.setStatus("Open"); // Default status to Open
        incidentRepository.save(incident);

        return "Incident created successfully!";
    }

    // ✅ Assign Team & Admin to an Incident
    public String assignTeamAndAdmin(Long id, Map<String, String> assignments) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        Long teamId = assignments.get("assignedTeam") != null && !assignments.get("assignedTeam").equals("Unassigned")
                ? Long.parseLong(assignments.get("assignedTeam")) : null;
        Long adminId = assignments.get("assignedAdmin") != null && !assignments.get("assignedAdmin").equals("Unassigned")
                ? Long.parseLong(assignments.get("assignedAdmin")) : null;

        Team team = (teamId != null) ? teamRepository.findById(teamId).orElse(null) : null;
        User admin = (adminId != null) ? userRepository.findById(adminId).orElse(null) : null;

        if (teamId != null && team == null) {
            return "Team not found!";
        }

        if (adminId != null && (admin == null || !admin.getRole().equals("ROLE_ADMIN"))) {
            return "Invalid Admin selection!";
        }

        if (teamId != null) incident.setAssignedTeam(team);
        if (adminId != null) incident.setAssignedAdmin(admin);

        incidentRepository.save(incident);
        return "Incident successfully updated!";
    }


    // ✅ Update Severity
    public String updateSeverity(Long id, Map<String, String> request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        incident.setSeverityLevel(request.get("severityLevel"));
        incidentRepository.save(incident);

        return "Severity level updated successfully!";
    }

    // ✅ Update Status
    public String updateStatus(Long id, Map<String, String> request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        incident.setStatus(request.get("status"));
        incidentRepository.save(incident);

        return "Status updated successfully!";
    }

    // ✅ Get all incidents
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    // ✅ Delete an Incident
    public String deleteIncident(Long id) {
        incidentRepository.deleteById(id);
        return "Incident deleted successfully!";
    }
    // ✅ Get Admins by Team
    public List<User> getAdminsByTeam(Long teamId) {
        return userRepository.findByRoleAndTeamId("ROLE_ADMIN", teamId);
    }

    public List<IncidentMatchResult> searchSimilarIncidents(String incidentNumber) {
        Incident incident = incidentRepository.findByIncidentNumber(incidentNumber);
        if (incident == null) {
            throw new RuntimeException("Incident not found!");
        }
        List<Incident> allIncidents = incidentRepository.findAll();
        return incidentSearchService.searchSimilarIncidents(incident.getIncidentNumber(), allIncidents);
    }

    public String resolveIncident(Long id, String resolutionText) {
        // 1. Fetch the incident
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        // 2. Set the resolution text
        incident.setResolution(resolutionText);

        // 3. Change status to "Resolved"
        incident.setStatus("Resolved");

        // 4. Save changes
        incidentRepository.save(incident);
        return "Incident resolved successfully!";
    }

}
