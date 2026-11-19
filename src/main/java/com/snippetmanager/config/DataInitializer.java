package com.snippetmanager.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetmanager.model.Snippet;
import com.snippetmanager.repository.SnippetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedIndustrySnippets(SnippetRepository snippetRepository) {
        return args -> {
            if (snippetRepository.count() < 10) {
                try (InputStream is = new ClassPathResource("seed-snippets.json").getInputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Snippet> seeds = mapper.readValue(is, new TypeReference<List<Snippet>>() {});
                    for (Snippet s : seeds) {
                        s.setId(null); // let MySQL auto-increment
                        snippetRepository.save(s);
                    }
                }
            }
        };
    }
}
