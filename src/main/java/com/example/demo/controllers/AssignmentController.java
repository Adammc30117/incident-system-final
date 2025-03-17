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

@RestController
@RequestMapping("/api/assignments")
@PreAuthorize("hasRole('ADMIN')")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AdminService adminService;


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

    @GetMapping("/admins/by-team/{teamId}")
    public ResponseEntity<List<User>> getAdminsByTeam(@PathVariable Long teamId) {
        List<User> admins = adminService.getAdminsByTeam(teamId);
        return ResponseEntity.ok(admins);
    }

}
