package com.snippetmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SnippetRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Programming language is required")
    private String language;

    @NotBlank(message = "Category is required")
    private String category;

    private String tags;
    private String description;

    @NotBlank(message = "Code content is required")
    private String codeContent;

    private String author = "Team Developer";
    private boolean favorite = false;
    private boolean sharedWithTeam = true;

    public SnippetRequestDTO() {
    }

    public SnippetRequestDTO(String title, String language, String category, String tags,
                             String description, String codeContent, String author, boolean favorite, boolean sharedWithTeam) {
        this.title = title;
        this.language = language;
        this.category = category;
        this.tags = tags;
        this.description = description;
        this.codeContent = codeContent;
        this.author = author;
        this.favorite = favorite;
        this.sharedWithTeam = sharedWithTeam;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCodeContent() { return codeContent; }
    public void setCodeContent(String codeContent) { this.codeContent = codeContent; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public boolean isSharedWithTeam() { return sharedWithTeam; }
    public void setSharedWithTeam(boolean sharedWithTeam) { this.sharedWithTeam = sharedWithTeam; }
}
