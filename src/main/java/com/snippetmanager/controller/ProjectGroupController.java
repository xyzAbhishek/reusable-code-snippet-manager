package com.snippetmanager.controller;

import com.snippetmanager.exception.ResourceNotFoundException;
import com.snippetmanager.model.ProjectGroup;
import com.snippetmanager.repository.ProjectGroupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class ProjectGroupController {

    private final ProjectGroupRepository groupRepository;

    public ProjectGroupController(ProjectGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProjectGroup>> getAllGroups() {
        return ResponseEntity.ok(groupRepository.findAll());
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProjectGroup>> getMyGroups(@RequestParam String username) {
        return ResponseEntity.ok(groupRepository.findByMembersContainingIgnoreCase(username));
    }

    @PostMapping
    public ResponseEntity<ProjectGroup> createGroup(@RequestBody Map<String, Object> body) {
        String name = String.valueOf(body.getOrDefault("name", "")).trim();
        String projectName = String.valueOf(body.getOrDefault("projectName", "")).trim();
        String description = String.valueOf(body.getOrDefault("description", "")).trim();
        String createdBy = String.valueOf(body.getOrDefault("createdBy", "member")).trim().toLowerCase();

        if (name.isBlank() || projectName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ProjectGroup group = new ProjectGroup();
        group.setName(name);
        group.setProjectName(projectName);
        group.setDescription(description);
        group.setCreatedBy(createdBy);

        List<String> members = new ArrayList<>();
        members.add(createdBy);
        if (body.get("members") instanceof List<?> extra) {
            for (Object m : extra) {
                String u = String.valueOf(m).trim().toLowerCase();
                if (!u.isBlank() && !members.contains(u)) {
                    members.add(u);
                }
            }
        }
        group.setMembers(members);
        return ResponseEntity.status(HttpStatus.CREATED).body(groupRepository.save(group));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectGroup> addMember(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ProjectGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        String username = body.getOrDefault("username", "").trim().toLowerCase();
        if (!username.isBlank() && !group.getMembers().contains(username)) {
            group.getMembers().add(username);
            group = groupRepository.save(group);
        }
        return ResponseEntity.ok(group);
    }

    @DeleteMapping("/{id}/members/{username}")
    public ResponseEntity<ProjectGroup> removeMember(@PathVariable Long id, @PathVariable String username) {
        ProjectGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        group.getMembers().removeIf(m -> m.equalsIgnoreCase(username));
        return ResponseEntity.ok(groupRepository.save(group));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<ProjectGroup> leaveGroup(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ProjectGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        String username = body.getOrDefault("username", "").trim();
        group.getMembers().removeIf(m -> m.equalsIgnoreCase(username));
        return ResponseEntity.ok(groupRepository.save(group));
    }
}
