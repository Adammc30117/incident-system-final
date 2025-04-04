package com.example.demo.controllers;

import com.example.demo.models.User;
import com.example.demo.service.AdminService;
import com.example.demo.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller responsible for managing assignment operations.
 * Provides endpoints to assign incidents to teams and admins,
 * and to fetch admin users associated with specific teams.
 */
@RestController
@RequestMapping("/api/assignments")
@PreAuthorize("hasRole('ADMIN')")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AdminService adminService;

    /**
     * Assigns a team and an admin to a specified incident.
     *
     * @param id The unique identifier of the incident.
     * @param assignments A map containing team and admin IDs as strings.
     * @return ResponseEntity indicating the success or failure of the assignment operation.
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> assignTeamAndAdmin(@PathVariable Long id, @RequestBody Map<String, String> assignments) {
        try {
            Long teamId = assignments.get("assignedTeam") != null ? Long.parseLong(assignments.get("assignedTeam")) : null;
            Long adminId = assignments.get("assignedAdmin") != null ? Long.parseLong(assignments.get("assignedAdmin")) : null;

            assignmentService.assignTeamAndAdmin(id, teamId, adminId);
            return ResponseEntity.ok("Incident successfully updated!");

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid team or admin ID format!");
        }
    }

    /**
     * Retrieves a list of admin users associated with a specific team.
     *
     * @param teamId The unique identifier of the team.
     * @return ResponseEntity containing a list of admin users belonging to the team.
     */
    @GetMapping("/admins/by-team/{teamId}")
    public ResponseEntity<List<User>> getAdminsByTeam(@PathVariable Long teamId) {
        List<User> admins = adminService.getAdminsByTeam(teamId);
        return ResponseEntity.ok(admins);
    }

}
