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

import static org.mockito.ArgumentMatchers.any;
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

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(snippetController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/snippets should return list of snippets")
    void shouldReturnAllSnippets() throws Exception {
        Snippet snippet = new Snippet(
                1L, "Binary Search", "Java", "Algorithms", "search,array",
                "Iterative binary search", "int binarySearch(int[] arr, int x) {}", "Dev", true, true
        );
        when(snippetService.getAllSnippets(null, null, null)).thenReturn(List.of(snippet));

        mockMvc.perform(get("/api/snippets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Binary Search"))
                .andExpect(jsonPath("$[0].language").value("Java"));
    }

    @Test
    @DisplayName("POST /api/snippets should create snippet and return 201 Created")
    void shouldCreateSnippetEndpoint() throws Exception {
        SnippetRequestDTO dto = new SnippetRequestDTO(
                "Docker Compose MySQL", "YAML", "DevOps & Docker", "docker,mysql",
                "MySQL 8 service definition", "services:\n  mysql:\n    image: mysql:8.0", "DevOps", false, true
        );
        Snippet saved = new Snippet(
                2L, dto.getTitle(), dto.getLanguage(), dto.getCategory(), dto.getTags(),
                dto.getDescription(), dto.getCodeContent(), dto.getAuthor(), false, true
        );
        when(snippetService.createSnippet(any(SnippetRequestDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/snippets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Docker Compose MySQL"));
    }
}
