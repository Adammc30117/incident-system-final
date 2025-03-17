package com.example.demo.repositories;

import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    List<User> findByRole(String role);
    Optional<User> findById(Long id);

    // ✅ Fix: Proper method to fetch admins
    List<User> findByRoleAndTeamId(String role, Long teamId);
}
