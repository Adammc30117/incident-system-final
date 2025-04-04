package com.example.demo.models;

import jakarta.persistence.*;

/**
 * Entity representing a team within the organization.
 * Used to assign incidents to specific departments like IT, HR, or Support.
 */
@Entity
public class Team {

    /** Unique identifier for the team */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name of the team (e.g., IT, HR, Support) */
    @Column(nullable = false)
    private String name;

    /** @return The unique ID of the team */
    public Long getId() {
        return id;
    }

    /** @return The name of the team */
    public String getName() {
        return name;
    }
}
