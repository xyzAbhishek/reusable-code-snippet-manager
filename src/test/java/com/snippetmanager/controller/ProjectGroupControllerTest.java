package com.snippetmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetmanager.model.ProjectGroup;
import com.snippetmanager.repository.ProjectGroupRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProjectGroupControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectGroupRepository groupRepository;

    @InjectMocks
    private ProjectGroupController groupController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ProjectGroup sampleGroup;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
        sampleGroup = new ProjectGroup();
        sampleGroup.setId(1L);
        sampleGroup.setName("Backend Core");
        sampleGroup.setProjectName("E-Commerce API");
        sampleGroup.setCreatedBy("abhishek");
        sampleGroup.setMembers(new ArrayList<>(List.of("abhishek", "achal")));
    }

    @Test
    @DisplayName("POST /api/groups - Create a new project group")
    void shouldCreateGroup() throws Exception {
        when(groupRepository.save(any(ProjectGroup.class))).thenReturn(sampleGroup);

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Backend Core",
                                "projectName", "E-Commerce API",
                                "createdBy", "abhishek"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Backend Core"));
    }

    @Test
    @DisplayName("GET /api/groups/my - List groups a member belongs to")
    void shouldListMyGroups() throws Exception {
        when(groupRepository.findByMembersContainingIgnoreCase("abhishek")).thenReturn(List.of(sampleGroup));

        mockMvc.perform(get("/api/groups/my").param("username", "abhishek"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectName").value("E-Commerce API"));
    }

    @Test
    @DisplayName("POST /api/groups/{id}/members - Add member to group")
    void shouldAddMemberToGroup() throws Exception {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(sampleGroup));
        when(groupRepository.save(any(ProjectGroup.class))).thenReturn(sampleGroup);

        mockMvc.perform(post("/api/groups/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "rohan"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(3));
    }

    @Test
    @DisplayName("POST /api/groups/{id}/leave - Member leaves group")
    void shouldLeaveGroup() throws Exception {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(sampleGroup));
        when(groupRepository.save(any(ProjectGroup.class))).thenReturn(sampleGroup);

        mockMvc.perform(post("/api/groups/1/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "achal"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1));
    }
}
