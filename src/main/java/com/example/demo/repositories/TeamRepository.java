package com.example.demo.repositories;

import com.example.demo.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for accessing and managing Team entities.
 * Provides methods to find teams by ID or name.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * Finds a team by its unique ID.
     *
     * @param id the ID of the team
     * @return an Optional containing the team if found, or empty otherwise
     */
    Optional<Team> findById(Long id);

    /**
     * Finds a team by its name.
     *
     * @param name the name of the team
     * @return an Optional containing the team if found, or empty otherwise
     */
    Optional<Team> findByName(String name);
}
