INSERT INTO snippets (title, language, category, tags, description, code_content, author, favorite, shared_with_team, share_token, copy_count, created_at, updated_at)
SELECT 'Spring Boot Global Exception Handler', 'Java', 'Backend & REST API', 'spring-boot,exception,rest-api,validation',
       'Centralized @RestControllerAdvice exception handler for validation and 404 errors in Spring Boot 3.',
       '@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
}',
       'Aarav Sharma', true, true, 'sb-exc-001', 42, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM snippets WHERE share_token = 'sb-exc-001');

INSERT INTO snippets (title, language, category, tags, description, code_content, author, favorite, shared_with_team, share_token, copy_count, created_at, updated_at)
SELECT 'Debounced Search Input Hook / Utility', 'JavaScript', 'Frontend & DOM', 'debounce,performance,search,events',
       'Lightweight utility function to debounce rapid input events before firing REST API search calls.',
       'function debounce(fn, delay = 300) {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}',
       'Priya Nair', true, true, 'js-deb-002', 68, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM snippets WHERE share_token = 'js-deb-002');

INSERT INTO snippets (title, language, category, tags, description, code_content, author, favorite, shared_with_team, share_token, copy_count, created_at, updated_at)
SELECT 'MySQL Indexed Full-Text Search Query', 'SQL', 'Database & Queries', 'mysql,indexing,search,optimization',
       'Optimized MySQL query using composite indexes and LOWER() pattern matching for tag and title lookup.',
       'SELECT id, title, language, category, tags, copy_count
FROM snippets
WHERE LOWER(title) LIKE CONCAT('%', LOWER(:keyword), '%')
   OR LOWER(tags) LIKE CONCAT('%', LOWER(:keyword), '%')
ORDER BY updated_at DESC
LIMIT 50;',
       'Rohan Verma', false, true, 'sql-idx-003', 29, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM snippets WHERE share_token = 'sql-idx-003');
