package com.example.demo.controllers;

import com.example.demo.models.Incident;
import com.example.demo.models.User;
import com.example.demo.models.Team;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.TeamRepository;
import com.example.demo.search.IncidentSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private IncidentSearchService incidentSearchService;  // Use the new IncidentSearchService

    @Autowired
    private UserRepository userRepository;  // For fetching admins

    @Autowired
    private TeamRepository teamRepository;  // For fetching teams

    // Create an incident
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> createIncident(@RequestBody Incident incident, Principal principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body("User not authenticated!");
        }

        incident.setCreatedBy(principal.getName());
        incident.setSeverityLevel(incident.getSeverityLevel() == null ? "Low" : incident.getSeverityLevel());
        incident.setStatus("Open"); // Default status to Open
        incidentRepository.save(incident);

        return ResponseEntity.ok("Incident created successfully!");
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignTeamAndAdmin(@PathVariable Long id, @RequestBody Map<String, String> assignments) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        // Get team ID and admin ID from the frontend (ensure it's in the correct format)
        String assignedTeamId = assignments.get("assignedTeam");
        String assignedAdminId = assignments.get("assignedAdmin");

        // Check if the values are valid
        if (assignedTeamId == null || assignedAdminId == null) {
            return ResponseEntity.badRequest().body("Team or Admin ID is missing!");
        }

        try {
            Long teamId = Long.parseLong(assignedTeamId);
            Long adminId = Long.parseLong(assignedAdminId);

            Optional<Team> team = teamRepository.findById(teamId);
            User admin = userRepository.findById(adminId).orElse(null);

            // Ensure both team and admin are retrieved correctly
            if (team.isEmpty() || admin == null || !admin.getRole().equals("ROLE_ADMIN")) {
                return ResponseEntity.badRequest().body("Team or Admin not found, or Admin is not valid!");
            }

            // Set the team and admin to the incident
            incident.setAssignedTeam(team.get());
            incident.setAssignedAdmin(admin);

            incidentRepository.save(incident);
            return ResponseEntity.ok("Incident successfully assigned to team and admin!");
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid team or admin ID format!");
        }
    }

    // Update Severity Level
    @PutMapping("/{id}/severity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateSeverity(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        incident.setSeverityLevel(request.get("severityLevel"));
        incidentRepository.save(incident);

        return ResponseEntity.ok("Severity level updated successfully!");
    }

    // Update Status (Open, Ongoing, Closed)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        incident.setStatus(request.get("status"));
        incidentRepository.save(incident);

        return ResponseEntity.ok("Status updated successfully!");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();  // Use the default method to fetch all incidents
    }

    // Delete an incident
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteIncident(@PathVariable Long id) {
        incidentRepository.deleteById(id);
        return ResponseEntity.ok("Incident deleted successfully!");
    }

    // Similarity Search Endpoint (using incident number)
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchSimilarIncidents(@RequestParam String incidentNumber) {
        Incident incident = incidentRepository.findByIncidentNumber(incidentNumber);
        if (incident == null) {
            return ResponseEntity.badRequest().body("Incident not found!");
        }

        List<Incident> allIncidents = incidentRepository.findAll();
        List<String> results = incidentSearchService.searchSimilarIncidents(incident.getIncidentNumber(), allIncidents);

        return ResponseEntity.ok(results);
    }
}
