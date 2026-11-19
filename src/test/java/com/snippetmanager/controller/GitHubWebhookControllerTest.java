package com.snippetmanager.controller;

import com.snippetmanager.repository.SnippetRepository;
import com.snippetmanager.service.GitHubSyncService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GitHubWebhookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GitHubSyncService gitHubSyncService;

    @Mock
    private SnippetRepository snippetRepository;

    @InjectMocks
    private GitHubWebhookController webhookController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webhookController).build();
    }

    @Test
    @DisplayName("POST /api/webhooks/github - Ignore self-sync commits to prevent loops")
    void shouldIgnoreSelfSyncCommit() throws Exception {
        when(gitHubSyncService.verifyWebhookSignature(anyString(), anyString())).thenReturn(true);
        String payload = "{\"head_commit\": {\"message\": \"[SnippetVault-Sync] Save: Test\"}}";

        mockMvc.perform(post("/api/webhooks/github")
                        .header("X-GitHub-Event", "push")
                        .header("X-Hub-Signature-256", "sha256=valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Ignored self-sync commit"));
    }

    @Test
    @DisplayName("POST /api/webhooks/github - Reject invalid webhook signature with 401")
    void shouldRejectInvalidSignature() throws Exception {
        when(gitHubSyncService.verifyWebhookSignature(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/webhooks/github")
                        .header("X-GitHub-Event", "push")
                        .header("X-Hub-Signature-256", "sha256=bad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid webhook signature"));
    }
}
