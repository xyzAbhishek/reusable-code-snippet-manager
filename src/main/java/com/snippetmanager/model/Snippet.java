package com.snippetmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "snippets")
public class Snippet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String title;

    @NotBlank(message = "Programming language is required")
    @Column(nullable = false, length = 50)
    private String language;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 255)
    private String tags;

    @Column(length = 500)
    private String description;

    @NotBlank(message = "Code content is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String codeContent;

    @Column(nullable = false, length = 100)
    private String author = "Team Developer";

    @Column(nullable = false)
    private boolean favorite = false;

    @Column(nullable = false)
    private boolean sharedWithTeam = true;

    @Column(unique = true, length = 64)
    private String shareToken;

    @Column(nullable = false)
    private int copyCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Snippet() {
    }

    public Snippet(Long id, String title, String language, String category, String tags,
                   String description, String codeContent, String author, boolean favorite, boolean sharedWithTeam) {
        this.id = id;
        this.title = title;
        this.language = language;
        this.category = category;
        this.tags = tags;
        this.description = description;
        this.codeContent = codeContent;
        this.author = author != null ? author : "Team Developer";
        this.favorite = favorite;
        this.sharedWithTeam = sharedWithTeam;
        this.shareToken = UUID.randomUUID().toString().substring(0, 12);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.shareToken == null || this.shareToken.isBlank()) {
            this.shareToken = UUID.randomUUID().toString().substring(0, 12);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getShareToken() { return shareToken; }
    public void setShareToken(String shareToken) { this.shareToken = shareToken; }

    public int getCopyCount() { return copyCount; }
    public void setCopyCount(int copyCount) { this.copyCount = copyCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
