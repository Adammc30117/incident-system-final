package com.example.demo.repositories;

import com.example.demo.models.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for managing Incident entities.
 * Provides built-in and custom methods for querying incidents based on various criteria.
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    /**
     * Finds all incidents created by the specified username.
     *
     * @param createdBy the username of the incident creator
     * @return a list of incidents created by the user
     */
    List<Incident> findByCreatedBy(String createdBy);

    /**
     * Finds a single incident by its unique incident number.
     *
     * @param incidentNumber the unique identifier of the incident
     * @return the matching incident or null if not found
     */
    Incident findByIncidentNumber(String incidentNumber);

    /**
     * Finds incidents where the title or description contains a given keyword (case-insensitive).
     *
     * @param titleKeyword      the keyword to search in the title
     * @param descriptionKeyword the keyword to search in the description
     * @return a list of matching incidents
     */
    List<Incident> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String titleKeyword, String descriptionKeyword);

    /**
     * Retrieves all incidents along with their assigned team and admin using eager loading.
     *
     * @return a list of all incidents with their associated team and admin
     */
    @Query("SELECT i FROM Incident i LEFT JOIN FETCH i.assignedTeam LEFT JOIN FETCH i.assignedAdmin")
    List<Incident> findAllWithTeamAndAdmin();

    /**
     * Finds incidents filtered by status and team ID.
     * If either parameter is null, that filter is ignored.
     *
     * @param status the status to filter by (or null to ignore)
     * @param teamId the team ID to filter by (or null to ignore)
     * @return a list of matching incidents, ordered by creation time (most recent first)
     */
    @Query("""
    SELECT i
    FROM Incident i
    WHERE (:status IS NULL OR i.status = :status)
      AND (:teamId IS NULL OR i.assignedTeam.id = :teamId)
    ORDER BY i.createdAt DESC
    """)
    List<Incident> findByFilters(
            @Param("status") String status,
            @Param("teamId") Long teamId
    );
}
