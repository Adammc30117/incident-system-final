package com.example.demo.controllers;

import com.example.demo.models.Incident;
import com.example.demo.models.IncidentComment;
import com.example.demo.models.User;
import com.example.demo.repositories.IncidentCommentRepository;
import com.example.demo.repositories.IncidentRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * REST controller responsible for managing incident comments.
 * Allows adding new comments to incidents and retrieving existing comments.
 */
@RestController
@RequestMapping("/api/incidents")
public class IncidentCommentController {

    @Autowired
    private IncidentCommentRepository commentRepo;

    @Autowired
    private IncidentRepository incidentRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * Adds a new comment to a specific incident.
     *
     * @param id The unique identifier of the incident.
     * @param payload Request body containing the comment content.
     * @param principal Authenticated user adding the comment.
     * @return ResponseEntity indicating success or failure of adding the comment.
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, String> payload, Principal principal) {
        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Empty comment");
        }

        Incident incident = incidentRepo.findById(id).orElseThrow();
        User author = userRepo.findByUsername(principal.getName());

        IncidentComment comment = new IncidentComment();
        comment.setContent(content);
        comment.setIncident(incident);
        comment.setAuthor(author);

        commentRepo.save(comment);
        return ResponseEntity.ok("Comment added");
    }

    /**
     * Retrieves all comments associated with a specific incident, ordered by timestamp.
     *
     * @param id The unique identifier of the incident.
     * @return List of comments with content, author, and creation timestamp.
     */
    @GetMapping("/{id}/comments")
    public List<Map<String, String>> getComments(@PathVariable Long id) {
        return commentRepo.findByIncidentIdOrderByTimestampAsc(id).stream().map(comment -> Map.of(
                "content", comment.getContent(),
                "createdBy", comment.getAuthor().getUsername(),
                "createdAt", comment.getTimestamp().toString()
        )).toList();
    }
}
