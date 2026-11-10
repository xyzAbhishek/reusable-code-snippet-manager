package com.snippetmanager.service;

import com.snippetmanager.dto.SnippetRequestDTO;
import com.snippetmanager.exception.ResourceNotFoundException;
import com.snippetmanager.model.Snippet;
import com.snippetmanager.repository.SnippetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SnippetService {

    private final SnippetRepository snippetRepository;

    public SnippetService(SnippetRepository snippetRepository) {
        this.snippetRepository = snippetRepository;
    }

    public List<Snippet> getAllSnippets(String keyword, String language, String category) {
        return snippetRepository.searchSnippets(keyword, language, category);
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
        return snippetRepository.save(snippet);
    }

    public Snippet updateSnippet(Long id, SnippetRequestDTO dto) {
        Snippet existing = getSnippetById(id);
        mapDtoToEntity(dto, existing);
        return snippetRepository.save(existing);
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
        snippet.setTags(dto.getTags());
        snippet.setDescription(dto.getDescription());
        snippet.setCodeContent(dto.getCodeContent());
        snippet.setAuthor(dto.getAuthor() != null && !dto.getAuthor().isBlank() ? dto.getAuthor() : "Team Developer");
        snippet.setFavorite(dto.isFavorite());
        snippet.setSharedWithTeam(dto.isSharedWithTeam());
    }
}
