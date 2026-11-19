package com.snippetmanager.service;

import com.snippetmanager.dto.SnippetRequestDTO;
import com.snippetmanager.exception.ResourceNotFoundException;
import com.snippetmanager.model.Snippet;
import com.snippetmanager.repository.SnippetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SnippetService {

    private final SnippetRepository snippetRepository;
    private GitHubSyncService gitHubSyncService;

    private static final Map<String, List<String>> AI_INTENT_EXPANSIONS = Map.ofEntries(
            Map.entry("ecommerce", List.of("pagination", "redis", "cache", "jwt", "validation", "mysql")),
            Map.entry("auth", List.of("jwt", "bcrypt", "security", "cors", "token", "secrets")),
            Map.entry("login", List.of("jwt", "bcrypt", "security", "token")),
            Map.entry("api", List.of("rest-api", "exception", "validation", "cors", "rate-limiter")),
            Map.entry("microservice", List.of("spring-boot", "webclient", "docker", "kubernetes", "retry", "logging")),
            Map.entry("dashboard", List.of("debounce", "fetch", "bootstrap", "csv", "dark-mode", "react")),
            Map.entry("frontend", List.of("debounce", "fetch", "clipboard", "xss", "bootstrap", "css")),
            Map.entry("database", List.of("mysql", "jpa", "index", "pagination", "fulltext", "window-function")),
            Map.entry("deploy", List.of("docker", "docker-compose", "github-actions", "kubernetes", "nginx")),
            Map.entry("analytics", List.of("groupingby", "window-function", "cte", "pandas", "rolling-average"))
    );

    public SnippetService(SnippetRepository snippetRepository) {
        this.snippetRepository = snippetRepository;
    }

    @Autowired(required = false)
    public void setGitHubSyncService(GitHubSyncService gitHubSyncService) {
        this.gitHubSyncService = gitHubSyncService;
    }

    public List<Snippet> getAllSnippets(String keyword, String language, String category) {
        boolean hasOperators = keyword != null && (keyword.contains(":") || keyword.trim().contains(" "));
        if (!hasOperators) {
            return snippetRepository.searchSnippets(keyword, language, category);
        }

        List<Snippet> baseList = snippetRepository.searchSnippets(null, language, category);
        String[] tokens = keyword.trim().toLowerCase().split("\\s+");

        return baseList.stream()
                .map(s -> new AbstractMap.SimpleEntry<>(s, scoreSnippet(s, tokens)))
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> aiSearchForProject(String projectQuery) {
        if (projectQuery == null || projectQuery.isBlank()) {
            return List.of();
        }
        String lowerQuery = projectQuery.toLowerCase();
        Set<String> expandedTerms = new LinkedHashSet<>(Arrays.asList(lowerQuery.split("[^a-z0-9+.#-]+")));
        expandedTerms.removeIf(t -> t.length() < 2);

        for (Map.Entry<String, List<String>> entry : AI_INTENT_EXPANSIONS.entrySet()) {
            if (lowerQuery.contains(entry.getKey())) {
                expandedTerms.addAll(entry.getValue());
            }
        }

        List<Snippet> all = snippetRepository.findAll();
        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (Snippet s : all) {
            int score = 0;
            List<String> matchedReasons = new ArrayList<>();
            String title = safeLower(s.getTitle());
            String tags = safeLower(s.getTags());
            String lang = safeLower(s.getLanguage());
            String cat = safeLower(s.getCategory());
            String desc = safeLower(s.getDescription());
            String proj = safeLower(s.getProjectContext());

            for (String term : expandedTerms) {
                if (title.contains(term) || tags.contains(term)) {
                    score += 10;
                    matchedReasons.add(term);
                } else if (lang.contains(term) || cat.contains(term) || proj.contains(term)) {
                    score += 7;
                    matchedReasons.add(term);
                } else if (desc.contains(term)) {
                    score += 4;
                }
            }
            if (score > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("snippet", s);
                item.put("score", score);
                item.put("reason", "Matched project needs: " + String.join(", ", matchedReasons.stream().distinct().limit(4).toList()));
                recommendations.add(item);
            }
        }

        recommendations.sort((a, b) -> Integer.compare((int) b.get("score"), (int) a.get("score")));
        return recommendations.stream().limit(12).collect(Collectors.toList());
    }

    private int scoreSnippet(Snippet s, String[] tokens) {
        int total = 0;
        String title = safeLower(s.getTitle());
        String lang = safeLower(s.getLanguage());
        String cat = safeLower(s.getCategory());
        String proj = safeLower(s.getProjectContext());
        String tags = safeLower(s.getTags());
        String desc = safeLower(s.getDescription());
        String code = safeLower(s.getCodeContent());
        String author = safeLower(s.getAuthor());

        for (String tok : tokens) {
            if (tok.startsWith("lang:")) {
                String val = tok.substring(5);
                if (!val.isEmpty() && !lang.contains(val)) return 0;
                total += 15;
            } else if (tok.startsWith("tag:")) {
                String val = tok.substring(4);
                if (!val.isEmpty() && !tags.contains(val)) return 0;
                total += 14;
            } else if (tok.startsWith("cat:")) {
                String val = tok.substring(4);
                if (!val.isEmpty() && !cat.contains(val)) return 0;
                total += 12;
            } else if (tok.startsWith("project:")) {
                String val = tok.substring(8);
                if (!val.isEmpty() && !proj.contains(val)) return 0;
                total += 14;
            } else if (tok.startsWith("author:")) {
                String val = tok.substring(7);
                if (!val.isEmpty() && !author.contains(val)) return 0;
                total += 10;
            } else {
                int tokenScore = 0;
                if (title.contains(tok)) tokenScore += 12;
                if (tags.contains(tok)) tokenScore += 10;
                if (proj.contains(tok)) tokenScore += 8;
                if (lang.contains(tok) || cat.contains(tok)) tokenScore += 6;
                if (desc.contains(tok)) tokenScore += 5;
                if (code.contains(tok)) tokenScore += 3;
                if (author.contains(tok)) tokenScore += 2;
                if (tokenScore == 0) return 0;
                total += tokenScore;
            }
        }
        return total;
    }

    private String safeLower(String val) {
        return val == null ? "" : val.toLowerCase();
    }

    public Snippet getSnippetById(Long id) {
        return snippetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet not found with ID: " + id));
    }

    public Snippet getSnippetByShareToken(String shareToken) {
        return snippetRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ResourceNotFoundException("Shared snippet not found for token: " + shareToken));
    }

    public Snippet createSnippet(SnippetRequestDTO dto) {
        Snippet snippet = new Snippet();
        mapDtoToEntity(dto, snippet);
        Snippet saved = snippetRepository.save(snippet);
        if (gitHubSyncService != null) {
            gitHubSyncService.pushSnippetToGitHub(saved);
            saved = snippetRepository.save(saved);
        }
        return saved;
    }

    public Snippet updateSnippet(Long id, SnippetRequestDTO dto) {
        Snippet existing = getSnippetById(id);
        mapDtoToEntity(dto, existing);
        Snippet saved = snippetRepository.save(existing);
        if (gitHubSyncService != null) {
            gitHubSyncService.pushSnippetToGitHub(saved);
            saved = snippetRepository.save(saved);
        }
        return saved;
    }

    public Snippet forkSnippet(Long id, String newAuthor, String newProjectContext) {
        Snippet original = getSnippetById(id);
        Snippet forked = new Snippet();
        forked.setTitle(original.getTitle() + " (Fork)");
        forked.setLanguage(original.getLanguage());
        forked.setCategory(original.getCategory());
        forked.setProjectContext(newProjectContext != null && !newProjectContext.isBlank()
                ? newProjectContext : "New Project Adaptation");
        forked.setVisibility("PUBLIC");
        forked.setTags(original.getTags());
        forked.setDescription(original.getDescription());
        forked.setCodeContent(original.getCodeContent());
        forked.setAuthor(newAuthor != null && !newAuthor.isBlank() ? newAuthor : "Team Collaborator");
        forked.setFavorite(false);
        forked.setSharedWithTeam(true);
        Snippet saved = snippetRepository.save(forked);
        if (gitHubSyncService != null) {
            gitHubSyncService.pushSnippetToGitHub(saved);
            saved = snippetRepository.save(saved);
        }
        return saved;
    }

    public Snippet toggleFavorite(Long id) {
        Snippet existing = getSnippetById(id);
        existing.setFavorite(!existing.isFavorite());
        return snippetRepository.save(existing);
    }

    public Snippet incrementCopyCount(Long id) {
        Snippet existing = getSnippetById(id);
        existing.setCopyCount(existing.getCopyCount() + 1);
        return snippetRepository.save(existing);
    }

    public void deleteSnippet(Long id) {
        Snippet existing = getSnippetById(id);
        if (gitHubSyncService != null) {
            gitHubSyncService.deleteSnippetFromGitHub(existing);
        }
        snippetRepository.delete(existing);
    }

    public Map<String, Object> getDashboardStats() {
        List<Snippet> all = snippetRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSnippets", all.size());
        stats.put("favoriteCount", all.stream().filter(Snippet::isFavorite).count());
        stats.put("sharedCount", all.stream().filter(Snippet::isSharedWithTeam).count());
        stats.put("totalCopies", all.stream().mapToInt(Snippet::getCopyCount).sum());
        Map<String, Long> byLanguage = all.stream()
                .collect(Collectors.groupingBy(Snippet::getLanguage, Collectors.counting()));
        stats.put("languageBreakdown", byLanguage);
        return stats;
    }

    private void mapDtoToEntity(SnippetRequestDTO dto, Snippet snippet) {
        snippet.setTitle(dto.getTitle());
        snippet.setLanguage(dto.getLanguage());
        snippet.setCategory(dto.getCategory());
        snippet.setProjectContext(dto.getProjectContext() != null && !dto.getProjectContext().isBlank()
                ? dto.getProjectContext() : "General Utility");
        snippet.setVisibility(dto.getVisibility() != null && !dto.getVisibility().isBlank()
                ? dto.getVisibility().toUpperCase() : "PUBLIC");
        snippet.setGroupId(dto.getGroupId());
        snippet.setGroupName(dto.getGroupName());
        snippet.setOwnerUsername(dto.getOwnerUsername());
        snippet.setTags(dto.getTags());
        snippet.setDescription(dto.getDescription());
        snippet.setCodeContent(dto.getCodeContent());
        snippet.setAuthor(dto.getAuthor() != null && !dto.getAuthor().isBlank() ? dto.getAuthor() : "Team Developer");
        snippet.setFavorite(dto.isFavorite());
        snippet.setSharedWithTeam(dto.isSharedWithTeam());
    }
}
