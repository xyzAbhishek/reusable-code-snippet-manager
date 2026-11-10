package com.snippetmanager.service;

import com.snippetmanager.dto.SnippetRequestDTO;
import com.snippetmanager.exception.ResourceNotFoundException;
import com.snippetmanager.model.Snippet;
import com.snippetmanager.repository.SnippetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnippetServiceTest {

    @Mock
    private SnippetRepository snippetRepository;

    @InjectMocks
    private SnippetService snippetService;

    private Snippet sampleSnippet;

    @BeforeEach
    void setUp() {
        sampleSnippet = new Snippet(
                1L,
                "JWT Token Filter",
                "Java",
                "Security",
                "jwt,spring-security,auth",
                "Extracts and validates Bearer token from Authorization header",
                "public class JwtFilter extends OncePerRequestFilter { }",
                "Aarav Sharma",
                true,
                true
        );
    }

    @Test
    @DisplayName("Should create and return a new code snippet")
    void shouldCreateSnippet() {
        SnippetRequestDTO dto = new SnippetRequestDTO(
                "JWT Token Filter", "Java", "Security", "jwt,spring-security",
                "Validates JWT", "public class JwtFilter {}", "Aarav Sharma", true, true
        );
        when(snippetRepository.save(any(Snippet.class))).thenReturn(sampleSnippet);

        Snippet created = snippetService.createSnippet(dto);

        assertNotNull(created);
        assertEquals("JWT Token Filter", created.getTitle());
        assertEquals("Java", created.getLanguage());
        verify(snippetRepository, times(1)).save(any(Snippet.class));
    }

    @Test
    @DisplayName("Should return snippet when valid ID is provided")
    void shouldGetSnippetById() {
        when(snippetRepository.findById(1L)).thenReturn(Optional.of(sampleSnippet));

        Snippet found = snippetService.getSnippetById(1L);

        assertEquals(1L, found.getId());
        assertEquals("Security", found.getCategory());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when snippet ID does not exist")
    void shouldThrowExceptionWhenIdNotFound() {
        when(snippetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> snippetService.getSnippetById(99L));
    }

    @Test
    @DisplayName("Should increment snippet copy count")
    void shouldIncrementCopyCount() {
        when(snippetRepository.findById(1L)).thenReturn(Optional.of(sampleSnippet));
        when(snippetRepository.save(any(Snippet.class))).thenReturn(sampleSnippet);

        Snippet updated = snippetService.incrementCopyCount(1L);

        assertEquals(1, updated.getCopyCount());
        verify(snippetRepository, times(1)).save(sampleSnippet);
    }

    @Test
    @DisplayName("Should search snippets by keyword, language, and category")
    void shouldSearchSnippets() {
        when(snippetRepository.searchSnippets("jwt", "Java", "Security"))
                .thenReturn(List.of(sampleSnippet));

        List<Snippet> results = snippetService.getAllSnippets("jwt", "Java", "Security");

        assertEquals(1, results.size());
        assertEquals("JWT Token Filter", results.get(0).getTitle());
    }
}
