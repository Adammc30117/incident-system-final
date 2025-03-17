package com.example.demo.dto;

public class IncidentMatchResult {
    private String incidentNumber;
    private String title;
    private String description;
    private String severityLevel;
    private String status;
    private String assignedTeamName;
    private String assignedAdminUsername;
    private double matchPercentage;

    public IncidentMatchResult(String incidentNumber, String title, String description,
                               String severityLevel, String status, String assignedTeamName,
                               String assignedAdminUsername, double matchPercentage) {
        this.incidentNumber = incidentNumber;
        this.title = title;
        this.description = description;
        this.severityLevel = severityLevel;
        this.status = status;
        this.assignedTeamName = assignedTeamName;
        this.assignedAdminUsername = assignedAdminUsername;
        this.matchPercentage = matchPercentage;
    }

    // Getters and setters (you can use Lombok @Data if preferred)
    public String getIncidentNumber() { return incidentNumber; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSeverityLevel() { return severityLevel; }
    public String getStatus() { return status; }
    public String getAssignedTeamName() { return assignedTeamName; }
    public String getAssignedAdminUsername() { return assignedAdminUsername; }
    public double getMatchPercentage() { return matchPercentage; }
}
