package com.snippetmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetmanager.dto.SnippetRequestDTO;
import com.snippetmanager.exception.GlobalExceptionHandler;
import com.snippetmanager.model.Snippet;
import com.snippetmanager.service.SnippetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SnippetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SnippetService snippetService;

    @InjectMocks
    private SnippetController snippetController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Snippet sampleSnippet;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(snippetController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        sampleSnippet = new Snippet(
                1L, "JWT Auth Filter", "Java", "Security & Auth", "jwt,spring-security",
                "Validates Bearer token", "public class JwtFilter {}", "Abhishek", true, true
        );
        sampleSnippet.setVisibility("GLOBAL");
    }

    @Test
    @DisplayName("GET /api/snippets - Search and list snippets")
    void shouldReturnAllSnippets() throws Exception {
        when(snippetService.getAllSnippets(null, null, null)).thenReturn(List.of(sampleSnippet));

        mockMvc.perform(get("/api/snippets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("JWT Auth Filter"))
                .andExpect(jsonPath("$[0].visibility").value("GLOBAL"));
    }

    @Test
    @DisplayName("POST /api/snippets - Create new snippet")
    void shouldCreateSnippet() throws Exception {
        SnippetRequestDTO dto = new SnippetRequestDTO(
                "JWT Auth Filter", "Java", "Security & Auth", "jwt,spring-security",
                "Validates Bearer token", "public class JwtFilter {}", "Abhishek", true, true
        );
        when(snippetService.createSnippet(any(SnippetRequestDTO.class))).thenReturn(sampleSnippet);

        mockMvc.perform(post("/api/snippets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("JWT Auth Filter"));
    }

    @Test
    @DisplayName("PUT /api/snippets/{id} - Update snippet")
    void shouldUpdateSnippet() throws Exception {
        SnippetRequestDTO dto = new SnippetRequestDTO(
                "Updated JWT Filter", "Java", "Security & Auth", "jwt,auth",
                "Updated description", "public class JwtFilterV2 {}", "Abhishek", false, true
        );
        sampleSnippet.setTitle("Updated JWT Filter");
        when(snippetService.updateSnippet(eq(1L), any(SnippetRequestDTO.class))).thenReturn(sampleSnippet);

        mockMvc.perform(put("/api/snippets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated JWT Filter"));
    }

    @Test
    @DisplayName("POST /api/snippets/{id}/fork - Fork snippet for another project")
    void shouldForkSnippet() throws Exception {
        Snippet forked = new Snippet(
                2L, "JWT Auth Filter (Fork)", "Java", "Security & Auth", "jwt,spring-security",
                "Validates Bearer token", "public class JwtFilter {}", "Achal", false, true
        );
        when(snippetService.forkSnippet(1L, "Achal", "Billing API")).thenReturn(forked);

        mockMvc.perform(post("/api/snippets/1/fork")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("author", "Achal", "projectContext", "Billing API"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("JWT Auth Filter (Fork)"));
    }

    @Test
    @DisplayName("PATCH /api/snippets/{id}/favorite - Toggle star favorite")
    void shouldToggleFavorite() throws Exception {
        sampleSnippet.setFavorite(false);
        when(snippetService.toggleFavorite(1L)).thenReturn(sampleSnippet);

        mockMvc.perform(patch("/api/snippets/1/favorite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(false));
    }

    @Test
    @DisplayName("POST /api/snippets/{id}/copy - Increment copy counter")
    void shouldIncrementCopyCount() throws Exception {
        sampleSnippet.setCopyCount(5);
        when(snippetService.incrementCopyCount(1L)).thenReturn(sampleSnippet);

        mockMvc.perform(post("/api/snippets/1/copy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.copyCount").value(5));
    }

    @Test
    @DisplayName("POST /api/snippets/ai-search - AI project snippet recommendations")
    void shouldReturnAiProjectRecommendations() throws Exception {
        when(snippetService.aiSearchForProject("Spring Boot JWT ecommerce"))
                .thenReturn(List.of(Map.of("snippet", sampleSnippet, "score", 20, "reason", "Matched project needs: jwt")));

        mockMvc.perform(post("/api/snippets/ai-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("query", "Spring Boot JWT ecommerce"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(20))
                .andExpect(jsonPath("$[0].snippet.title").value("JWT Auth Filter"));
    }

    @Test
    @DisplayName("GET /api/snippets/share/{token} - Lookup shared snippet")
    void shouldGetSnippetByShareToken() throws Exception {
        sampleSnippet.setShareToken("sb-exc-001");
        when(snippetService.getSnippetByShareToken("sb-exc-001")).thenReturn(sampleSnippet);

        mockMvc.perform(get("/api/snippets/share/sb-exc-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareToken").value("sb-exc-001"));
    }

    @Test
    @DisplayName("DELETE /api/snippets/{id} - Delete snippet")
    void shouldDeleteSnippet() throws Exception {
        doNothing().when(snippetService).deleteSnippet(1L);

        mockMvc.perform(delete("/api/snippets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Snippet deleted successfully"));
    }
}
