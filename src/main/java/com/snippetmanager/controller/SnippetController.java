package com.snippetmanager.controller;

import com.snippetmanager.dto.SnippetRequestDTO;
import com.snippetmanager.model.Snippet;
import com.snippetmanager.service.SnippetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/snippets")
public class SnippetController {

    private final SnippetService snippetService;

    public SnippetController(SnippetService snippetService) {
        this.snippetService = snippetService;
    }

    @GetMapping
    public ResponseEntity<List<Snippet>> getAllSnippets(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(snippetService.getAllSnippets(keyword, language, category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Snippet> getSnippetById(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.getSnippetById(id));
    }

    @GetMapping("/share/{shareToken}")
    public ResponseEntity<Snippet> getSnippetByShareToken(@PathVariable String shareToken) {
        return ResponseEntity.ok(snippetService.getSnippetByShareToken(shareToken));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(snippetService.getDashboardStats());
    }

    @PostMapping
    public ResponseEntity<Snippet> createSnippet(@Valid @RequestBody SnippetRequestDTO dto) {
        Snippet created = snippetService.createSnippet(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Snippet> updateSnippet(
            @PathVariable Long id,
            @Valid @RequestBody SnippetRequestDTO dto) {
        return ResponseEntity.ok(snippetService.updateSnippet(id, dto));
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<Snippet> toggleFavorite(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.toggleFavorite(id));
    }

    @PostMapping("/{id}/copy")
    public ResponseEntity<Snippet> incrementCopyCount(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.incrementCopyCount(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSnippet(@PathVariable Long id) {
        snippetService.deleteSnippet(id);
        return ResponseEntity.ok(Map.of("message", "Snippet deleted successfully"));
    }
}
