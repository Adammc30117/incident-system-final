package com.example.demo.repositories;

import com.example.demo.models.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Fetch incidents created by a specific user
    List<Incident> findByCreatedBy(String createdBy);

    // Fetch incident by incidentNumber
    Incident findByIncidentNumber(String incidentNumber);

    // Fetch all incidents with assignedTeam and assignedAdmin relationships eagerly loaded
    @Query("SELECT i FROM Incident i LEFT JOIN FETCH i.assignedTeam LEFT JOIN FETCH i.assignedAdmin")
    List<Incident> findAllWithTeamAndAdmin();
}
