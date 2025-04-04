package com.example.demo.dto;

/**
 * DTO representing the result of a similarity match between incidents.
 * Contains metadata such as matching percentage and key incident details.
 */
public class IncidentMatchResult {
    private String incidentNumber;
    private String title;
    private String description;
    private String severityLevel;
    private String status;
    private String assignedTeamName;
    private String assignedAdminUsername;
    private double matchPercentage;
    private String resolution;

    /**
     * Constructs a new IncidentMatchResult object.
     *
     * @param incidentNumber        Unique identifier of the matched incident.
     * @param title                 Title of the matched incident.
     * @param description           Description of the matched incident.
     * @param severityLevel         Severity level (e.g., Low, Medium, High).
     * @param status                Current status of the incident (e.g., Open, Resolved).
     * @param assignedTeamName      Name of the team assigned to the incident.
     * @param assignedAdminUsername Username of the admin assigned to the incident.
     * @param matchPercentage       Similarity match percentage.
     * @param resolution            Resolution details of the matched incident.
     */
    public IncidentMatchResult(String incidentNumber, String title, String description,
                               String severityLevel, String status, String assignedTeamName,
                               String assignedAdminUsername, double matchPercentage, String resolution) {
        this.incidentNumber = incidentNumber;
        this.title = title;
        this.description = description;
        this.severityLevel = severityLevel;
        this.status = status;
        this.assignedTeamName = assignedTeamName;
        this.assignedAdminUsername = assignedAdminUsername;
        this.matchPercentage = matchPercentage;
        this.resolution = resolution;
    }

    /** @return The unique identifier of the incident. */
    public String getIncidentNumber() { return incidentNumber; }

    /** @return The title of the incident. */
    public String getTitle() { return title; }

    /** @return The full description of the incident. */
    public String getDescription() { return description; }

    /** @return The severity level of the incident. */
    public String getSeverityLevel() { return severityLevel; }

    /** @return The current status of the incident. */
    public String getStatus() { return status; }

    /** @return The name of the team assigned to the incident. */
    public String getAssignedTeamName() { return assignedTeamName; }

    /** @return The username of the admin assigned to the incident. */
    public String getAssignedAdminUsername() { return assignedAdminUsername; }

    /** @return The similarity match percentage. */
    public double getMatchPercentage() { return matchPercentage; }

    /** @return The resolution details of the incident. */
    public String getResolution() { return resolution; }
}
