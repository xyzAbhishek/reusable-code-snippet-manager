# Reusable Code Snippet Manager

A full-stack web platform where developers can save, organize, search, categorize, tag, copy, and share reusable code snippets across multiple programming languages.

## Tech Stack
- **Frontend**: HTML5, CSS3, JavaScript (ES6+), Bootstrap 5.3
- **Backend**: Java 17, Spring Boot 3.2 (Spring Web, Spring Data JPA, Bean Validation)
- **Database**: MySQL 8.0 (with H2 In-Memory database configured for automated JUnit 5 testing)
- **Build & Dependency Management**: Apache Maven
- **Testing**: JUnit 5 (Jupiter), Mockito, Spring MockMvc
- **DevOps & Containerization**: Docker, Docker Compose, GitHub Actions CI/CD
- **API Testing**: Postman Collection v2.1

## Project Directory Structure
```text
reusable-code-snippet-manager/
├── .github/workflows/ci-cd.yml                  # GitHub Actions CI/CD pipeline
├── postman/
│   └── Reusable_Code_Snippet_Manager.postman_collection.json
├── src/
│   ├── main/
│   │   ├── java/com/snippetmanager/
│   │   │   ├── SnippetManagerApplication.java   # Spring Boot entry point
│   │   │   ├── config/WebConfig.java            # CORS configuration
│   │   │   ├── controller/SnippetController.java# REST API endpoints
│   │   │   ├── dto/SnippetRequestDTO.java       # Validated request payload
│   │   │   ├── exception/                       # Global exception handling
│   │   │   ├── model/Snippet.java               # JPA entity mapped to MySQL
│   │   │   ├── repository/SnippetRepository.java# JPA search & filter queries
│   │   │   └── service/SnippetService.java      # Business logic & stats
│   │   └── resources/
│   │       ├── application.properties           # MySQL & JPA config
│   │       ├── schema.sql                       # Database DDL
│   │       ├── data.sql                         # Initial seed snippets
│   │       └── static/                          # HTML, CSS, JS, Bootstrap UI
│   │           ├── index.html
│   │           ├── css/styles.css
│   │           └── js/app.js
│   └── test/
│       ├── java/com/snippetmanager/
│       │   ├── controller/SnippetControllerTest.java
│       │   └── service/SnippetServiceTest.java
│       └── resources/application-test.properties
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Quick Start with Docker Compose
```bash
docker compose up --build -d
```
Then open `http://localhost:8080` in your browser.

## Running Locally with Maven & MySQL
1. Ensure Java 17, Maven 3.9+, and MySQL 8.0 are running.
2. Update `src/main/resources/application.properties` if your MySQL credentials differ from `root` / `rootpassword`.
3. Run tests and start the server:
```bash
mvn clean test
mvn spring-boot:run
```
