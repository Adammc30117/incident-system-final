package com.example.demo.controllers;

import com.example.demo.dto.IncidentMatchResult;
import com.example.demo.models.Incident;
import com.example.demo.models.User;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing incidents.
 * Provides functionality to create, update, retrieve, delete incidents,
 * perform incident searches, similarity searches, and manage assignments.
 */
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private IncidentRepository incidentRepository;

    /**
     * Creates a new incident.
     *
     * @param incident  Incident details provided by the user.
     * @param principal Currently authenticated user.
     * @return Response indicating the result of the creation operation.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> createIncident(@RequestBody Incident incident, Principal principal) {
        String response = incidentService.createIncident(incident, principal);
        return response.equals("Incident created successfully!")
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * Assigns a team and admin to an incident.
     *
     * @param id          Incident ID to update.
     * @param assignments Map containing team and admin assignments.
     * @return Response indicating the result of the assignment operation.
     */
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> assignTeamAndAdmin(@PathVariable Long id, @RequestBody Map<String, String> assignments) {
        String response = incidentService.assignTeamAndAdmin(id, assignments);
        return response.equals("Incident successfully updated!")
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * Updates the severity level of an incident.
     *
     * @param id      Incident ID to update.
     * @param request Request containing new severity level.
     * @return Response indicating success of severity update.
     */
    @PutMapping("/{id}/severity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateSeverity(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(incidentService.updateSeverity(id, request));
    }

    /**
     * Updates the status of an incident.
     *
     * @param id      Incident ID to update.
     * @param request Request containing new status.
     * @return Response indicating success of status update.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(incidentService.updateStatus(id, request));
    }

    /**
     * Retrieves incidents filtered by status, team, incident number, or keyword.
     *
     * @param status         Status filter (optional).
     * @param teamId         Team ID filter (optional).
     * @param incidentNumber Specific incident number (optional).
     * @param keyword        Keyword search in title/description (optional).
     * @return List of incidents matching filters.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Incident> getIncidents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String incidentNumber,
            @RequestParam(required = false) String keyword
    ) {
        if (incidentNumber != null && !incidentNumber.trim().isEmpty()) {
            Incident inc = incidentRepository.findByIncidentNumber(incidentNumber);
            return (inc != null) ? List.of(inc) : List.of();
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            return incidentRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        }

        String filterStatus = (status == null || status.equalsIgnoreCase("All")) ? null : status;
        Long filterTeamId = (teamId == null || teamId == 0) ? null : teamId;

        return incidentRepository.findByFilters(filterStatus, filterTeamId);
    }

    /**
     * Retrieves incidents created by the currently authenticated user.
     *
     * @param principal Currently authenticated user.
     * @return List of incidents created by the user.
     */
    @GetMapping("/my-incidents")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Incident> getUserIncidents(Principal principal) {
        return incidentRepository.findByCreatedBy(principal.getName());
    }

    /**
     * Deletes an incident by ID.
     *
     * @param id Incident ID to delete.
     * @return Response indicating success of delete operation.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteIncident(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.deleteIncident(id));
    }

    /**
     * Performs a similarity search based on a provided incident number.
     *
     * @param incidentNumber Incident number to compare against.
     * @return List of incidents that are similar to the provided incident.
     */
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

    /**
     * Retrieves a single incident by its unique incident number.
     *
     * @param incidentNumber The incident number of the incident to fetch.
     * @return Incident details or a 404 response if not found.
     */
    @GetMapping("/{incidentNumber}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getIncidentByNumber(@PathVariable String incidentNumber) {
        Incident incident = incidentRepository.findByIncidentNumber(incidentNumber);
        if (incident == null) {
            return ResponseEntity.status(404).body("Incident not found.");
        }
        return ResponseEntity.ok(incident);
    }

    /**
     * Retrieves admin users associated with a specific team.
     *
     * @param teamId Team ID for which admins are retrieved.
     * @return List of admin users belonging to the team.
     */
    @GetMapping("/admins/by-team/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAdminsByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(incidentService.getAdminsByTeam(teamId));
    }

    /**
     * Marks an incident as resolved and sets its resolution details.
     *
     * @param id      Incident ID to resolve.
     * @param request Request containing resolution details.
     * @return Response indicating success or failure of resolution operation.
     */
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
