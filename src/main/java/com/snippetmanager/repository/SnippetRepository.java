package com.snippetmanager.repository;

import com.snippetmanager.model.Snippet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnippetRepository extends JpaRepository<Snippet, Long> {

    List<Snippet> findByLanguageIgnoreCaseOrderByUpdatedAtDesc(String language);

    List<Snippet> findByCategoryIgnoreCaseOrderByUpdatedAtDesc(String category);

    List<Snippet> findByFavoriteTrueOrderByUpdatedAtDesc();

    List<Snippet> findBySharedWithTeamTrueOrderByUpdatedAtDesc();

    Optional<Snippet> findByShareToken(String shareToken);

    @Query("SELECT s FROM Snippet s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.tags) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.codeContent) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:language IS NULL OR :language = '' OR LOWER(s.language) = LOWER(:language)) AND " +
           "(:category IS NULL OR :category = '' OR LOWER(s.category) = LOWER(:category)) " +
           "ORDER BY s.updatedAt DESC")
    List<Snippet> searchSnippets(@Param("keyword") String keyword,
                                 @Param("language") String language,
                                 @Param("category") String category);
}
