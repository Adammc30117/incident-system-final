package com.example.demo.repositories;

import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing User entities.
 * Provides methods for user lookup by username, role, team, and ID.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the username to search for
     * @return the matching user, or null if not found
     */
    User findByUsername(String username);

    /**
     * Retrieves all users with a specific role.
     *
     * @param role the role to filter by (e.g., ROLE_ADMIN, ROLE_USER)
     * @return a list of users with the given role
     */
    List<User> findByRole(String role);

    /**
     * Finds a user by their unique ID.
     *
     * @param id the user ID
     * @return an Optional containing the user if found, or empty otherwise
     */
    Optional<User> findById(Long id);

    /**
     * Retrieves all users with a specific role assigned to a specific team.
     *
     * @param role   the role to filter by (e.g., ROLE_ADMIN)
     * @param teamId the ID of the team
     * @return a list of users matching the role and team
     */
    List<User> findByRoleAndTeamId(String role, Long teamId);
}
