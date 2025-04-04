package com.example.demo.models;

import jakarta.persistence.*;

/**
 * Entity representing a system user, which can be a regular user or an admin.
 * Admin users may be associated with a specific team.
 */
@Entity
public class User {

    /** Unique identifier for the user */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique username used for authentication */
    @Column(nullable = false, unique = true)
    private String username;

    /** Password used for user authentication */
    @Column(nullable = false)
    private String password;

    /** Role of the user (e.g., ROLE_USER, ROLE_ADMIN) */
    @Column(nullable = false)
    private String role;

    /** Team assigned to the user (applicable only for admins) */
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    // Getters and Setters

    /** @return The unique ID of the user */
    public Long getId() {
        return id;
    }

    /** @param id Sets the user's ID */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return The username of the user */
    public String getUsername() {
        return username;
    }

    /** @param username Sets the user's username */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return The user's password */
    public String getPassword() {
        return password;
    }

    /** @param password Sets the user's password */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @return The role assigned to the user */
    public String getRole() {
        return role;
    }

    /** @param role Sets the user's role */
    public void setRole(String role) {
        this.role = role;
    }

    /** @return The team assigned to the user (if applicable) */
    public Team getTeam() {
        return team;
    }

    /** @param team Sets the user's assigned team */
    public void setTeam(Team team) {
        this.team = team;
    }
}
