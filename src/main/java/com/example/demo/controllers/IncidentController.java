package com.example.demo.controllers;

import com.example.demo.dto.IncidentMatchResult;
import com.example.demo.models.Incident;
import com.example.demo.models.User;
import com.example.demo.models.Team;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.TeamRepository;
import com.example.demo.search.IncidentSearchService;
import com.example.demo.service.IncidentService;
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
    private IncidentService incidentService;

    // ✅ Create an Incident
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> createIncident(@RequestBody Incident incident, Principal principal) {
        String response = incidentService.createIncident(incident, principal);
        return response.equals("Incident created successfully!")
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    // ✅ Assign Team & Admin
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignTeamAndAdmin(@PathVariable Long id, @RequestBody Map<String, String> assignments) {
        String response = incidentService.assignTeamAndAdmin(id, assignments);
        return response.equals("Incident successfully updated!")
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }


    // ✅ Update Severity
    @PutMapping("/{id}/severity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateSeverity(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(incidentService.updateSeverity(id, request));
    }

    // ✅ Update Status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(incidentService.updateStatus(id, request));
    }

    // ✅ Get All Incidents
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Incident> getAllIncidents() {
        return incidentService.getAllIncidents();
    }

    // ✅ Delete an Incident
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteIncident(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.deleteIncident(id));
    }

    // Similarity Search Endpoint (using incident number)
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> searchSimilarIncidents(@RequestParam String incidentNumber) {
        try {
            List<IncidentMatchResult> results = incidentService.searchSimilarIncidents(incidentNumber);
            if (results.isEmpty()) {
                return ResponseEntity.badRequest().body("No similar incidents found.");
            }
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // ✅ Get Admins by Team
    @GetMapping("/admins/by-team/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAdminsByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(incidentService.getAdminsByTeam(teamId));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> resolveIncident(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String resolutionText = request.get("resolution");
        String response = incidentService.resolveIncident(id, resolutionText);

        if (response.equals("Incident resolved successfully!")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }


}
