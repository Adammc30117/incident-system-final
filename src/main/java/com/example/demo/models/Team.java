package com.example.demo.models;

import jakarta.persistence.*;

@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Team name (e.g., IT, HR, Support)

    // ✅ Add this getter for `id`
    public Long getId() {
        return id;
    }

    public String getName(){
        return name;
    }
}
