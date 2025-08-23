package com.gustasousagh.studify.estudify.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustasousagh.studify.estudify.integrations.dto.ModuleDTO;
import com.gustasousagh.studify.estudify.integrations.dto.ModulesWrapperDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GeminiResponseParser {

    private final ObjectMapper objectMapper;

    public Flux<ModuleDTO> extractModules(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            String modulesJsonText = root
                    .path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text")
                    .asText();

            ModulesWrapperDTO wrapper = objectMapper.readValue(modulesJsonText, ModulesWrapperDTO.class);
            List<ModuleDTO> modules = wrapper.getModules();
            return Flux.fromIterable(modules == null ? List.of() : modules);
        } catch (Exception e) {
            return Flux.error(new RuntimeException("Falha ao processar roadmap do Gemini", e));
        }
    }

    public String fallbackContentJson() {
        return "{\"content\":\"[conteúdo não retornado pelo provedor]\"}";
    }

    public record ContentExtraction(String content, String rawText, boolean parsed, String errorMessage) {}

    public ContentExtraction extractContentBestEffort(String responseJson) {
        try {
            if (responseJson == null || responseJson.isBlank()) {
                return new ContentExtraction("[conteúdo não retornado pelo provedor]", null, false, "responseJson vazio");
            }

            JsonNode root = objectMapper.readTree(responseJson);

            String raw = root.path("candidates").path(0)
                    .path("content").path(0)
                    .asText(null);

            if (raw == null) {
                raw = root.path("candidates").path(0)
                        .path("content").path("parts").path(0)
                        .path("text").asText();
            }

            String rawText = ContentCleaner.stripCodeFence(raw);
            if (rawText == null || rawText.isBlank()) {
                return new ContentExtraction("[conteúdo vazio]", null, false, "rawText vazio");
            }

            try {
                var dto = objectMapper.readValue(rawText, com.gustasousagh.studify.estudify.integrations.dto.CourseTopicDTO.class);
                String content = dto.getContent();
                if (content == null || content.isBlank()) {
                    return new ContentExtraction(rawText, rawText, false, "DTO sem campo content");
                }
                return new ContentExtraction(content, rawText, true, null);

            } catch (Exception parseErr) {
                return new ContentExtraction(rawText, rawText, false,
                        "falha ao parsear DTO: " + parseErr.getClass().getSimpleName());
            }

        } catch (Exception fatal) {
            return new ContentExtraction("[falha ao processar conteúdo]", null, false, fatal.toString());
        }
    }
}