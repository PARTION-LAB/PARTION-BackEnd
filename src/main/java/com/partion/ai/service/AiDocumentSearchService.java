package com.partion.ai.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
public class AiDocumentSearchService {

    private static final String GUIDE_DOCUMENT_PATH = "ai-docs/partion-service-guide.md";
    private static final int MAX_CONTEXT_SECTION_COUNT = 3;

    public String searchRelevantContext(String question) {
        String document = loadDocument();
        List<String> sections = splitSections(document);

        return sections.stream()
                .filter(section -> calculateScore(section, question) > 0)
                .sorted((first, second) ->
                        Integer.compare(
                                calculateScore(second, question),
                                calculateScore(first, question)
                        )
                )
                .limit(MAX_CONTEXT_SECTION_COUNT)
                .reduce((first, second) -> first + "\n\n" + second)
                .orElse("관련 문서를 찾지 못했습니다.");
    }

    private String loadDocument() {
        try {
            ClassPathResource resource = new ClassPathResource(GUIDE_DOCUMENT_PATH);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("AI 서비스 가이드 문서를 읽을 수 없습니다.", e);
        }
    }

    private List<String> splitSections(String document) {
        return Arrays.stream(document.split("(?=## )"))
                .map(String::trim)
                .filter(section -> !section.isBlank())
                .toList();
    }

    private int calculateScore(String section, String question) {
        List<String> keywords = extractKeywords(question);

        int score = 0;

        for (String keyword : keywords) {
            if (section.contains(keyword)) {
                score++;
            }
        }

        return score;
    }

    private List<String> extractKeywords(String question) {
        return Arrays.stream(question.split("[\\s?,.!]+"))
                .map(String::trim)
                .filter(word -> word.length() >= 2)
                .toList();
    }
}