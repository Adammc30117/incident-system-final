package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Getter and Setter for name
    @Column(nullable = false)
    private String name; // Team name (e.g., IT, HR, Support)


    public String getName(){
        return name;
    }

}
