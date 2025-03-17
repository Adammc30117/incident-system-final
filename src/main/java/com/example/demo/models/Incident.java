package com.example.demo.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String incidentNumber;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;


    private String status = "Open"; // ✅ Default to "Open"

    private String severityLevel = "Low"; // ✅ Default to "Low"

    @Column(nullable = false)
    private String createdBy; // Tracks the user who created the incident

    @ManyToOne
    @JoinColumn(name = "assigned_team_id")
    private Team assignedTeam; // Reference to Team

    @ManyToOne
    @JoinColumn(name = "assigned_admin_id")
    private User assignedAdmin; // Reference to User (Admin)

    // Constructor
    public Incident() {
        // Automatically generate a unique incident number
        this.incidentNumber = "INC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIncidentNumber() {
        return incidentNumber;
    }

    public void setIncidentNumber(String incidentNumber) {
        this.incidentNumber = incidentNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel;
    }


    public Team getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(Team assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    public User getAssignedAdmin() {
        return assignedAdmin;
    }

    public void setAssignedAdmin(User assignedAdmin) {
        this.assignedAdmin = assignedAdmin;
    }
}
