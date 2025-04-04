package com.example.demo.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing an incident submitted by a user.
 * Stores details such as title, description, status, severity level,
 * assignment info, resolution, and creation metadata.
 */
@Entity
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique identifier for the incident, e.g., INCXXXXXXX */
    @Column(nullable = false, unique = true)
    private String incidentNumber;

    /** Short title describing the incident */
    @Column(nullable = false)
    private String title;

    /** Detailed description of the issue */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Explanation of how the issue was resolved */
    @Column(columnDefinition = "TEXT")
    private String resolution;

    /** Current status of the incident (e.g., Open, Resolved) */
    private String status = "Open";

    /** Severity level of the incident (e.g., Low, Medium, High) */
    private String severityLevel = "Low";

    /** Username of the user who submitted the incident */
    @Column(nullable = false)
    private String createdBy;

    /** List of comments associated with the incident */
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentComment> comments = new ArrayList<>();

    /** Team assigned to the incident */
    @ManyToOne
    @JoinColumn(name = "assigned_team_id")
    private Team assignedTeam;

    /** Admin user assigned to handle the incident */
    @ManyToOne
    @JoinColumn(name = "assigned_admin_id")
    private User assignedAdmin;

    /** Timestamp marking when the incident was created */
    @Column(name = "created_at", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;

    /**
     * Default constructor that auto-generates a unique incident number.
     */
    public Incident() {
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

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
