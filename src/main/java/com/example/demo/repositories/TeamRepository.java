package com.example.demo.repositories;

import com.example.demo.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findById(Long id);
    Optional<Team> findByName(String name);
}
