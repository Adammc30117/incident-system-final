package com.example.demo.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity representing a comment made on an incident.
 * Contains the comment content, author, timestamp, and the related incident.
 */
@Entity
public class IncidentComment {

    /** Unique identifier for the comment */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The text content of the comment */
    private String content;

    /** The incident that this comment is associated with */
    @ManyToOne
    @JoinColumn(name = "incident_id")
    private Incident incident;

    /** The user (admin) who authored the comment */
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    /** Timestamp of when the comment was created */
    private LocalDateTime timestamp = LocalDateTime.now();

    // Getters and Setters

    /** @return The unique identifier of the comment */
    public Long getId() {
        return id;
    }

    /** @param id Sets the comment ID */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return The content of the comment */
    public String getContent() {
        return content;
    }

    /** @param content Sets the comment content */
    public void setContent(String content) {
        this.content = content;
    }

    /** @return The incident associated with this comment */
    public Incident getIncident() {
        return incident;
    }

    /** @param incident Sets the incident this comment belongs to */
    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    /** @return The author (user) of the comment */
    public User getAuthor() {
        return author;
    }

    /** @param author Sets the author (user) of the comment */
    public void setAuthor(User author) {
        this.author = author;
    }

    /** @return The timestamp when the comment was created */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /** @param timestamp Sets the creation timestamp of the comment */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
