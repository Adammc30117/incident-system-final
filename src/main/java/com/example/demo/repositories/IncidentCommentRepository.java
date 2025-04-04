package com.example.demo.repositories;

import com.example.demo.models.IncidentComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing IncidentComment entities.
 * Provides methods to retrieve comments associated with specific incidents.
 */
public interface IncidentCommentRepository extends JpaRepository<IncidentComment, Long> {

    /**
     * Retrieves all comments for a given incident, ordered by timestamp (ascending).
     *
     * @param incidentId The ID of the incident.
     * @return List of incident comments sorted by creation time.
     */
    List<IncidentComment> findByIncidentIdOrderByTimestampAsc(Long incidentId);
}
